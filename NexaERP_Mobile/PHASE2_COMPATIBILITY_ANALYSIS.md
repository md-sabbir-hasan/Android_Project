# Dashboard Phase 2 Compatibility Analysis

Analysis date: 2026-07-30  
Scope: current `backend/` and `NexaERP_Mobile/` source trees  
Change policy: inspection only; this report is the only file added.

## Executive conclusion

The current Android authentication and Dashboard Phase 1 implementation is compatible with the reconnected backend. The complete Android dashboard DTO graph matches the current backend DTO graph by JSON field name and compatible Java type. `GET /api/dashboard/summary` is available to every authenticated user and permission-filters its individual sections by returning `null`.

Phase 2 is ready for both requested features, with different levels of existing Android support:

- **Notification unread badge:** backend support is complete, but Android has no notification API, DTO, repository, state, or UI code. The unread-count payload is a scalar `Long` inside the normal `ApiResponse` envelope.
- **Recent activity:** backend support is already present in `GET /api/dashboard/summary` as `data.recentActivities`. Android already models and parses this field. Only permission-aware state/UI rendering and tests are missing. A separate recent-activity endpoint must not be invented.

No current breaking backend/Android contract mismatch was found. The important implementation risks are nullable permission-filtered sections, nullable activity properties, local date-time values without an offset/zone, primitive expense counters collapsing absent/null JSON to zero, and refresh/logout semantics described below.

## 1. Current backend authentication contract

### Mobile authentication endpoints

All responses use:

```json
{
  "success": true,
  "message": "string",
  "data": {}
}
```

Errors use the same shape with `success: false`; `data` is normally absent or `null`.

| Method and path | Authentication | Request | Success data |
|---|---|---|---|
| `POST /api/auth/login` | Public | `{ "email": String, "password": String }`; both required, email validated | `LoginResponseDto` |
| `POST /api/auth/refresh` | Public | `{ "refreshToken": String }`; nonblank | `LoginResponseDto` |
| `POST /api/auth/logout` | Bearer JWT required | No body | `null` |
| `GET /api/auth/me` | Bearer JWT required; no special authority | No body | `CurrentUserResponseDto` |

`LoginResponseDto`:

| Field | Backend type | Behavior |
|---|---|---|
| `accessToken` | `String` | JWT access token |
| `refreshToken` | `String` | Opaque UUID value |
| `expiresIn` | `Long` | Access-token lifetime in **milliseconds**, not an absolute timestamp |
| `userId` | `Long` | Authenticated user ID |
| `name` | `String` | User display name |
| `email` | `String` | User email |

`CurrentUserResponseDto` is `{ id: Long, name: String, email: String, status: String, roles: Set<String>, permissions: Set<String> }`. Roles and permissions are built from the current persisted user/role relationships, rather than trusted from Android state.

The mobile refresh endpoint validates the stored refresh token, its revoked flag, and its expiry. It issues a new access token but returns and continues using the **same refresh token**. It does not rotate the mobile refresh token. Mobile logout deletes all refresh tokens for the authenticated user, so logging out one mobile session invalidates every refresh session belonging to that user.

Spring Security is stateless. All non-public URLs require authentication. Protected calls use `Authorization: Bearer <accessToken>`. The security entry point returns HTTP 401 plus `ApiResponse.error("Authentication is required")`; access denial returns HTTP 403 plus `ApiResponse.error("Access is denied")`.

### Web authentication

`WebAuthController` exists under `/api/auth/web`:

- `POST /api/auth/web/login`
- `POST /api/auth/web/refresh`
- `POST /api/auth/web/logout`

These calls validate trusted browser origin and store the refresh token in an HTTP-only cookie. Their body is `ApiResponse<WebAuthResponseDto>`, whose data omits `refreshToken`. Web refresh rotates the cookie refresh token. This contract is for browsers and is correctly not used by Android.

### Android authentication compatibility

Android `ApiService`, `LoginRequest`, `LoginResponse`, `RefreshTokenRequest`, `CurrentUserResponse`, and generic `ApiResponse<T>` exactly match the mobile endpoints and envelope. `TokenManager` treats `expiresIn` as milliseconds and calculates an absolute expiry, which matches the backend.

`AuthInterceptor` adds the bearer token to protected calls. `TokenRefreshInterceptor` refreshes shortly before local expiry, and `TokenAuthenticator` retries once after a 401. `RefreshClient` deliberately has no auth/refresh interceptors, avoiding refresh recursion.

Compatibility cautions:

- Proactive refresh relies on the locally computed expiry. A changed device clock can cause early or late refresh; a late token still recovers through the 401 authenticator.
- On refresh, Android only updates the access token and expiry. This is compatible today because mobile refresh reuses the same refresh token. If the backend later adopts mobile refresh-token rotation, Android must persist the returned refresh token too.
- The backend mobile refresh path checks token validity but, unlike the web refresh path, does not visibly re-check that the user remains `ACTIVE`. A JWT/me call may ultimately reject an inactive user depending on the user-details implementation, but this is a backend policy concern rather than a current Android contract mismatch.
- A 403 from a protected resource does not expire the Android session; only invalid refresh responses (400/401/403) do. This is appropriate for ordinary permission denial.
- `MainActivity.logout()` sends the `Authorization` header explicitly while the interceptor also sets it. OkHttp `.header()` replaces rather than duplicates it, so the request remains valid.

## 2. Current dashboard contract

### Endpoint

`GET /api/dashboard/summary`

- Authentication: required.
- Method permission: `isAuthenticated()` only.
- Success envelope message: `"Dashboard summary loaded"`.
- Response: `ApiResponse<DashboardSummaryDto>`.
- Section authorization is applied inside `DashboardServiceImpl`; unauthorized sections/fields are returned as `null`, not rejected with 403.

### Exact DTO graph and permissions

| JSON field | Type and exact nested fields | Availability |
|---|---|---|
| `users` | `UserSummaryDto`: `total`, `active`, `pending`, `inactive`, `locked` (`Long`) | Whole section only with `MANAGE_USERS`; otherwise `null` |
| `security` | `SecuritySummaryDto`: `totalRoles`, `totalPermissions` (`Long`) | Section exists if user has `MANAGE_ROLES` or `MANAGE_PERMISSIONS`. Each count is independently `null` without its corresponding permission |
| `finance` | `FinanceSummaryDto`: `totalAccounts`, `totalJournalEntries`, `postedJournalEntries`, `draftJournalEntries`, `reversedJournalEntries` (`Long`) | Section exists with `VIEW_ACCOUNTS` or `VIEW_JOURNAL`. `totalAccounts` requires the former; journal fields require the latter |
| `business` | See below | Section exists with any of `VIEW_BANKING`, `VIEW_INVOICE`, `VIEW_VENDOR_BILL`, `VIEW_REPORT`; individual groups remain `null` without their permission |
| `system` | `SystemSummaryDto`: `applicationVersion: String`, `serverTime: LocalDateTime`, `serverTimezone: String`, `environment: String`, `javaVersion: String` | Only with `MANAGE_SETTINGS`; otherwise `null` |
| `recentActivities` | `List<RecentActivityDto>` | Only with `VIEW_AUDIT_LOGS`; otherwise `null`. Authorized but no logs yields `[]` |
| `budget` | `BudgetDashboardDto` | Only with `VIEW_BUDGET_REPORT`; otherwise `null` |
| `expense` | `ExpenseDashboardDto` | Only with `VIEW_EXPENSE`; otherwise `null` |

`BusinessSummaryDto`:

| Field | Type | Permission/behavior |
|---|---|---|
| `cashPosition` | `BigDecimal` | `VIEW_BANKING`; may be `null` when cash is not configured |
| `cashConfigured` | `Boolean` | `VIEW_BANKING`; `false` when cash-flow calculation reports missing configuration |
| `asOfDate` | `LocalDate` | Set whenever the business section exists |
| `currencyCode` | `String` | Set by successful banking calculation; may be `null` otherwise |
| `accountsReceivable` | `BigDecimal` | `VIEW_INVOICE` |
| `overdueInvoiceCount` | `Long` | `VIEW_INVOICE` |
| `overdueInvoiceAmount` | `BigDecimal` | `VIEW_INVOICE` |
| `accountsPayable` | `BigDecimal` | `VIEW_VENDOR_BILL` |
| `overdueBillCount` | `Long` | `VIEW_VENDOR_BILL` |
| `overdueBillAmount` | `BigDecimal` | `VIEW_VENDOR_BILL` |
| `revenueTrend` | `List<MonthlyTrendDto>` | `VIEW_REPORT`; exactly six month entries |
| `expenseTrend` | `List<MonthlyTrendDto>` | `VIEW_REPORT`; exactly six month entries |
| `trendFromDate` | `LocalDate` | `VIEW_REPORT` |
| `trendToDate` | `LocalDate` | `VIEW_REPORT` |

`MonthlyTrendDto` is `{ month: String, amount: BigDecimal }`; `month` is English `"MMM yyyy"`.

`BudgetDashboardDto`:

```text
hasActiveBudget: boolean
activeBudgetId: Long
activeBudgetName: String
unavailableReason: String
fromDate: LocalDate
toDate: LocalDate
currencyCode: String
totalExpenseBudget: BigDecimal
totalExpenseActualYtd: BigDecimal
expenseUtilizationPercent: BigDecimal
totalRevenueBudget: BigDecimal
totalRevenueActualYtd: BigDecimal
revenueAchievementPercent: BigDecimal
topAccounts: List<BudgetTopAccountDto>
```

`BudgetTopAccountDto` is `{ accountId: Long, accountCode: String, accountName: String, budgetAmount: BigDecimal, actualAmount: BigDecimal, utilizationPercent: BigDecimal }`.

When no active fiscal year/budget/current period exists, `budget` is not `null`; it contains `hasActiveBudget: false`, a non-null `unavailableReason`, and `topAccounts: []`, while the other budget fields remain `null`.

`ExpenseDashboardDto`:

```text
draftCount: long
draftTotalAmount: BigDecimal
postedThisMonthTotal: BigDecimal
recurringActiveCount: long
recurringDueSoonCount: long
outstandingDue: BigDecimal
```

The recurring counters are populated only with `VIEW_RECURRING_EXPENSE`; otherwise Java primitive defaults serialize as `0`. Thus zero means either “permitted and none” or “not permitted,” unless Android also checks the current user's permissions.

## 3. Current notification contract

All notification endpoints require only authentication; there is no notification-specific seeded permission.

| Method and path | Parameters/body | Response data | Empty behavior |
|---|---|---|---|
| `GET /api/notifications/unread-count` | None | `Long` scalar count | `0`, not `null`, when none |
| `GET /api/notifications` | Query: `page=0`, `size=20`, `unreadOnly=false` defaults | `PageResponseDto<NotificationResponseDto>` | `content: []`; standard zero-element page metadata |
| `PATCH /api/notifications/{id}/read` | Path `id: Long`; no body | `NotificationResponseDto` | Idempotent for an already-read owned notification; unknown or other-user ID is “Notification not found” |
| `PATCH /api/notifications/read-all` | No body | `null` | Succeeds when none are unread |

`PageResponseDto<T>` is:

```text
content: List<T>
page: int
size: int
totalElements: long
totalPages: int
first: boolean
last: boolean
```

`NotificationResponseDto`:

| Field | Type | Nullability/meaning |
|---|---|---|
| `id` | `Long` | Persisted notifications should have a value |
| `type` | `NotificationType` enum | Non-null entity field |
| `title` | `String` | Non-null entity field |
| `message` | `String` | Non-null entity field |
| `route` | `String` | Nullable |
| `entityType` | `String` | Nullable |
| `entityId` | `Long` | Nullable |
| `read` | `boolean` | Derived from `readAt != null` |
| `readAt` | `LocalDateTime` | Nullable until read |
| `expiresAt` | `LocalDateTime` | Nullable |
| `createdAt` | `LocalDateTime` | Audited persisted value; expected non-null |

Current enum values are `SYSTEM`, `USER_INVITATION`, `INVOICE_OVERDUE`, `INVOICE_PAYMENT`, `VENDOR_BILL_DUE`, `VENDOR_BILL_PAYMENT`, `BUDGET_WARNING`, `BUDGET_EXCEEDED`, `ACCOUNTING_PERIOD`, `EXPENSE`, `RECURRING_EXPENSE`, `PAYMENT`, `BANKING`, and `FIXED_ASSET`.

The list is scoped to the authenticated user and ordered newest first. The repository methods do not visibly filter `expiresAt`, so expired notifications can still appear and count as unread unless removed elsewhere.

Android currently has **no notification API, response DTO, page DTO, repository, ViewModel/state, badge, list, or mark-read implementation**. The generic `ApiResponse<T>`, authenticated Retrofit client, refresh handling, and `LocalDateTimeAdapter` are reusable.

## 4. Current recent-activity source

The correct dashboard source is:

```text
GET /api/dashboard/summary
  -> ApiResponse<DashboardSummaryDto>
  -> data.recentActivities
```

The backend queries `AuditLogRepository.findTop10ByOrderByCreatedAtDesc()`, maps at most ten newest audit records, and returns:

```text
action: String
entityName: String
entityId: Long
userName: String
createdAt: LocalDateTime
description: String
```

The required permission is `VIEW_AUDIT_LOGS`. Without it, `recentActivities` is `null`. With it and no audit records, it is an empty list. `entityId` and `userName` can be `null` in the audit entity; `createdAt` is set on persist but is not declared database-non-null in `AuditLog`; UI code should therefore be defensive. `description` is generated as `"<ACTION> <ENTITY_NAME>"` and is normally non-null because action/entity name are non-null.

Separate audit-log endpoints do exist under `/api/audit-logs`, all requiring `VIEW_AUDIT_LOGS`, but none is a general dashboard “recent activity” endpoint:

- `GET /api/audit-logs/entity/{entityName}/{entityId}`
- `GET /api/audit-logs/entity/{entityName}/{entityId}/timeline`
- `GET /api/audit-logs/user/{userId}`
- `GET /api/audit-logs/entity/{entityName}`

They are record-, user-, or entity-type-specific and paged. They should not be used or reshaped into an invented Phase 2 endpoint. `data.recentActivities` is already the exact backend dashboard feature.

## 5. Android compatibility findings

### Exact matches

- Auth endpoint paths and HTTP methods match.
- Login, refresh, current-user, logout request/response models match.
- Android and backend `ApiResponse<T>` both use `success`, `message`, and `data`.
- Dashboard endpoint and envelope match exactly.
- `DashboardSummaryResponse` contains all eight current backend top-level fields with matching names.
- Every nested dashboard field matches, including money (`BigDecimal`), identifiers/counts (`Long`/`long`), `LocalDate`, `LocalDateTime`, booleans, and lists.
- `RecentActivityResponse` exactly matches `RecentActivityDto`.
- Android registers custom adapters for every dashboard `LocalDate` and `LocalDateTime`.
- `CurrentUserResponse` exactly matches `/api/auth/me`, including role and permission sets.
- Backend permission codes used by existing Android quick actions (`CREATE_INVOICE`, `CREATE_EXPENSE`, `CREATE_JOURNAL`, `CREATE_PAYMENT`, `CREATE_VENDOR_BILL`) are seeded.
- Nullable dashboard sections are generally checked before Phase 1 UI rendering.

### Missing DTO fields

No dashboard or authentication DTO fields are missing.

For Phase 2 notifications, the entire Android contract is missing: notification DTO, page DTO, unread-count API method, list and mark-read API methods, repository/state/UI models.

### Extra or obsolete Android fields

No extra or obsolete authentication/dashboard DTO fields were found. Gson's default unknown-field tolerance also makes additive backend fields non-breaking.

### Type mismatches

No direct backend/Android dashboard or auth type mismatch was found.

One semantic ambiguity remains: backend expense recurring counts are primitives and become `0` when the user lacks `VIEW_RECURRING_EXPENSE`; Android also uses primitive `long`, so it cannot distinguish missing/null/not-authorized from genuine zero based on the DTO alone.

### Endpoint mismatches

No implemented Android endpoint mismatch was found. Android simply has no notification endpoint declarations yet.

### Permission mismatches

- Android `PermissionCodes` contains only five create-action constants. It lacks `VIEW_AUDIT_LOGS` and all other dashboard visibility permissions. This does not break parsing or existing UI, because `PermissionEvaluator` accepts arbitrary strings, but Phase 2 should add/use an exact `VIEW_AUDIT_LOGS` constant rather than a typo-prone literal.
- Notification endpoints require authentication only. Android must not hide the unread badge behind a nonexistent notification permission.
- Dashboard summary itself requires authentication only; Android correctly calls it for every verified user.
- Recent activity must be treated as permitted only with `VIEW_AUDIT_LOGS`, matching both the backend null behavior and the current-user permission set.

### Nullable-field risks

- Every permission-filtered dashboard section may be `null`.
- Fields inside `security`, `finance`, and `business` may be independently `null` even when the parent section exists.
- `business.currencyCode` may be `null`; Android's money formatter must continue tolerating it.
- Most unavailable-budget properties are `null`.
- `recentActivities` is `null` when unauthorized and `[]` when authorized but empty. These states should not share the same user-facing message.
- Recent activity `entityId`, `userName`, and defensively `createdAt` must be treated as nullable.
- Notification `route`, `entityType`, `entityId`, `readAt`, and `expiresAt` are nullable.
- `CurrentUserResponse.roles` and `permissions` are expected sets but are not validation-annotated. `DashboardFragment.newInstance()` protects against null; other future consumers should do the same.

### LocalDate/LocalDateTime parsing risks

Current Spring serialization is expected to produce ISO values compatible with `LocalDate.parse()` and `LocalDateTime.parse()`, for example `2026-07-30` and `2026-07-30T10:15:30`. Existing Android tests cover these basic forms.

Risks:

- `LocalDateTime` contains no offset or zone. `serverTimezone` exists only in the system dashboard section, and activities/notifications do not carry a zone. Android cannot reliably convert these timestamps to the device time zone; Phase 2 should initially display them as server-local/unspecified local time or use relative labels without claiming cross-zone precision.
- The adapters are strict. A future backend change to append `Z` or `+06:00` would fail parsing because that is an `Instant`/`OffsetDateTime`, not `LocalDateTime`.
- Existing parsing tests do not cover fractional seconds, null activity timestamps, notification timestamps, malformed values, or offset-bearing timestamps.
- Android `minSdk` is 26, so `java.time` itself is available without core-library desugaring for the supported devices.

### ApiResponse envelope findings

The envelope matches exactly. Notification count is not `{ "count": n }`; it is directly:

```json
{ "success": true, "message": "OK", "data": 3 }
```

Notification list paging is inside `data`, not at the envelope root.

`DashboardRepository` correctly rejects a null envelope, `success: false`, and `success: true` with null `data`. It currently marks an application-level `success: false` as retryable regardless of cause; this is a UX classification issue, not a wire incompatibility.

### Dashboard runtime assessment

No currently rendered Phase 1 section should fail solely due to the backend reconnection:

- Financial overview checks `business` and `expense` for null and checks nullable monetary values.
- Attention checks nullable invoice/bill counts.
- Expense primitive counters are safe to read, though permission ambiguity can show no item rather than “not available.”

Potential runtime failures are limited to strict date parsing if backend timestamp formatting changes, malformed enum handling in future notification DTO design, or future UI code dereferencing nullable recent-activity/notification fields.

## 6. Breaking issues

No present breaking authentication or dashboard contract issue was found.

The following would block Phase 2 if ignored during implementation:

1. Android has no notification client contract. Calling or rendering notification data requires new Android-only API/DTO/repository/state code.
2. Recent activity must use nullable/empty/unauthorized handling. Treating `recentActivities == null` as an empty authorized feed would expose incorrect UX and permission semantics.
3. Phase 2 timestamp UI must not assume an offset or device-local instant; backend values are zone-less `LocalDateTime`.

## 7. Non-breaking issues

1. `PermissionCodes` is incomplete for dashboard visibility, especially `VIEW_AUDIT_LOGS`.
2. Recurring expense primitive zero values cannot distinguish lack of permission from an actual zero.
3. Dashboard `currencyCode` comes from banking/cash-flow data only; users with receivable/payable access but no banking access may see amounts without a currency code.
4. `DashboardUiState.isEmptyOrAccessLimited()` treats `recentActivities: []` as non-empty because the list is non-null. A user with only audit permission and no logs will not see the general access-limited empty state, which is logically defensible but requires a dedicated empty-activity message.
5. Repository application errors are always classified retryable.
6. Existing tests validate only a representative subset of the dashboard DTO graph and basic ISO date/time parsing.
7. Notification queries do not visibly exclude expired records.
8. Mobile logout revokes all of the user's refresh sessions, not only the current device.

## 8. Required fixes before Phase 2

No backend changes are required for either requested feature.

Required Android work:

1. Add the exact `VIEW_AUDIT_LOGS` permission constant and use `PermissionEvaluator` for recent-activity visibility/empty-state decisions.
2. Add a notification API with the exact existing backend paths and types:
   - `GET api/notifications/unread-count` -> `Call<ApiResponse<Long>>`
   - `GET api/notifications` with `page`, `size`, `unreadOnly`
   - `PATCH api/notifications/{id}/read`
   - `PATCH api/notifications/read-all`
3. Add Android `PageResponse<T>` and `NotificationResponse` models with nullable reference fields and a forward-compatible representation for `type` (a `String` is safer than a closed Java enum if backend enum values may grow).
4. Add repository/state error handling consistent with `DashboardRepository`, including session-expiry behavior inherited from `RetrofitClient`.
5. Render `data.recentActivities` from the existing dashboard response; do not make a second audit request and do not invent an endpoint.
6. Add tests for unread scalar envelope, notification page envelope, null notification fields, recent activity `null` versus `[]`, null activity fields, and fractional-second `LocalDateTime`.

## 9. Safe Phase 2 implementation scope

### Notification unread badge

- **Exact backend source:** `NotificationController` / `NotificationService.getUnreadCount()`.
- **Endpoint:** `GET /api/notifications/unread-count`.
- **Permission:** authentication only; no special permission code.
- **Response:** `ApiResponse<Long>`.
- **Empty/null behavior:** service returns primitive `long`, so no unread items returns `data: 0`. Android should still handle an unexpected null envelope/data defensively by hiding the badge or showing zero, not crashing.
- **Reusable Android code:** `RetrofitClient`, bearer/refresh interceptors, `ApiResponse<T>`, lifecycle patterns from the dashboard.
- **Missing backend support:** none.
- **Safe initial scope:** load count when the authenticated main/dashboard screen becomes active and after a deliberate refresh; display the badge only for a positive count. Avoid polling, push messaging, notification list navigation, or mark-read coupling until those behaviors are explicitly designed.

### Recent activity section

- **Exact backend source:** `DashboardServiceImpl.buildRecentActivities()` from the top ten newest audit logs.
- **Endpoint/field:** `GET /api/dashboard/summary` -> `data.recentActivities`.
- **Permission:** `VIEW_AUDIT_LOGS`.
- **Response:** nullable `List<RecentActivityDto>` inside the existing dashboard response.
- **Empty/null behavior:** `null` means unauthorized; `[]` means authorized with no activity; otherwise up to ten newest items.
- **Reusable Android code:** `DashboardApi`, `DashboardRepository`, `DashboardViewModel`, `DashboardUiState`, `DashboardSummaryResponse`, `RecentActivityResponse`, `LocalDateTimeAdapter`, current-user permissions passed into `DashboardFragment`.
- **Missing backend support:** none.
- **Safe initial scope:** add a permission-aware section to the existing dashboard rendering. Show it only when authorized; use a dedicated authorized-empty message for `[]`; tolerate null actor/entity ID/timestamp; do not add pagination or separate audit APIs.

## 10. Recommended implementation order

1. Lock contracts with Android unit tests for recent activity, unread count, notification paging, nullable fields, and time parsing.
2. Add `VIEW_AUDIT_LOGS` to Android permission constants and implement recent-activity rendering from the already-loaded dashboard payload. This is the smallest, lowest-risk Phase 2 slice and requires no extra network request.
3. Add notification/page DTOs and notification Retrofit declarations.
4. Add a focused notification repository and unread-count UI state.
5. Add the unread badge with lifecycle-safe loading and explicit `0`/null/error behavior.
6. Only after the badge is stable, add notification list and mark-one/read-all flows if they are included in the next approved scope.
7. Re-run Android unit tests and add UI/instrumentation coverage for permission-hidden, authorized-empty, populated, refresh, and session-expiry states.

## Inspected implementation areas

Backend: `AuthController`, `WebAuthController`, `AuthServiceImpl`, authentication DTOs, `SecurityConfig`, `DashboardController`, `DashboardService`, `DashboardServiceImpl`, all dashboard DTOs, `NotificationController`, `NotificationService`, `NotificationServiceImpl`, notification entity/enum/DTO, audit controller/service/entity/timeline DTO, `ApiResponse`, `PageResponseDto`, and seeded permissions in `DataSeeder`.

Android: `RetrofitClient`, refresh client, auth/refresh interceptors and authenticator, date adapters, `ApiService`, `DashboardApi`, auth/dashboard/envelope DTOs, `DashboardRepository`, `DashboardViewModel`, `DashboardUiState`, `DashboardFragment`, `MainActivity`, `LoginActivity`, `SplashActivity`, `SessionManager`, `TokenManager`, permission helpers, build configuration, and existing unit/instrumentation test sources.
