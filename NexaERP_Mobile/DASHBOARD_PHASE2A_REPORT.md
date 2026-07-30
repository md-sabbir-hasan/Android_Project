# Dashboard Phase 2A Implementation Report

Implementation date: 2026-07-30  
Scope: Android notification unread badge and dashboard recent activity only  
Backend changes: none

## 1. Exact unread-count backend contract

Endpoint:

```http
GET /api/notifications/unread-count
Authorization: Bearer <access-token>
```

Authorization is `isAuthenticated()` only. There is no notification-specific permission.

The exact response type is `ApiResponse<Long>`. `Long` is the scalar value in `data`; the backend does not return an object with a `count` field:

```json
{
  "success": true,
  "message": "OK",
  "data": 5
}
```

The shared envelope fields are:

```text
success: boolean
message: String
data: T
```

The service returns a primitive `long` from the user-scoped unread query, so the normal empty result is `data: 0`. Android nevertheless handles null body, null data, `success: false`, HTTP failure, and network failure defensively.

No Android unread-count object DTO was created because the exact backend data contract is a scalar `Long`. `NotificationApi` therefore declares `Call<ApiResponse<Long>>`, avoiding an invented field.

## 2. Exact recent-activity contract used

Source:

```text
GET /api/dashboard/summary
  -> ApiResponse<DashboardSummaryDto>
  -> data.recentActivities
```

No audit-log endpoint is called.

`RecentActivityDto` / existing Android `RecentActivityResponse` fields:

```text
action: String
entityName: String
entityId: Long
userName: String
createdAt: LocalDateTime
description: String
```

Required permission: `VIEW_AUDIT_LOGS`.

Backend behavior:

- Permission absent: `recentActivities` is `null`.
- Permission present with no logs: `recentActivities` is `[]`.
- Permission present with logs: newest audit activities, with the backend supplying up to ten.

Android displays at most five.

## 3. Files created

Production:

- `app/src/main/java/com/nexaerp/mobile/data/remote/api/NotificationApi.java`
- `app/src/main/java/com/nexaerp/mobile/data/repository/NotificationRepository.java`
- `app/src/main/java/com/nexaerp/mobile/feature/dashboard/NotificationBadgeFormatter.java`
- `app/src/main/java/com/nexaerp/mobile/feature/dashboard/RecentActivityPresenter.java`
- `app/src/main/res/drawable/ic_notifications.xml`
- `app/src/main/res/drawable/notification_badge_background.xml`

Tests:

- `app/src/test/java/com/nexaerp/mobile/data/repository/NotificationRepositoryTest.java`
- `app/src/test/java/com/nexaerp/mobile/feature/dashboard/DashboardPhase2ATest.java`

Documentation:

- `DASHBOARD_PHASE2A_REPORT.md`

## 4. Files modified

- `app/src/main/java/com/nexaerp/mobile/core/permission/PermissionCodes.java`
- `app/src/main/java/com/nexaerp/mobile/feature/dashboard/DashboardFragment.java`
- `app/src/main/java/com/nexaerp/mobile/feature/dashboard/DashboardUiState.java`
- `app/src/main/java/com/nexaerp/mobile/feature/dashboard/DashboardViewModel.java`
- `app/src/main/java/com/nexaerp/mobile/feature/dashboard/DashboardViewModelFactory.java`
- `app/src/main/res/layout/fragment_dashboard.xml`
- `app/src/main/res/values/strings.xml`

No `backend/` file was modified.

## 5. Notification badge behavior

- A notification bell is displayed in the dashboard header.
- `null` or `0`: badge hidden.
- `1` through `99`: exact count shown.
- Greater than `99`: `99+`.
- Accessibility descriptions:
  - `No unread notifications`
  - `<n> unread notifications`
  - `More than 99 unread notifications`
- Tapping the bell shows `Notification Center is coming next.`
- No notification Activity, Fragment, list, pagination, or mark-read code was added.
- Count loads on initial dashboard load and pull-to-refresh.
- `onResume()` requests a count only when the last successful count is at least 60 seconds old. An in-flight guard prevents simultaneous duplicate count requests.

## 6. Recent Activity behavior

- Uses only the existing dashboard summary payload.
- The section is omitted without `VIEW_AUDIT_LOGS`.
- It is also omitted if the backend returned `null`.
- With permission and an empty backend list, the section shows `No recent activity`.
- With activity, it displays at most five read-only rows.
- Each row uses only backend-supported values:
  - readable action and entity name
  - entity ID as a reference when present
  - user name when present
  - localized timestamp when present
- Missing optional values are omitted safely.
- Missing timestamp displays `Time unavailable`.
- Known create/login, delete/cancel, and update/edit action families receive suitable local icons. Unknown action/entity values use a generic information icon and readable text fallback.
- No synthetic customer, amount, status, route, navigation, or “View all” data was added.

The backend timestamp is a zone-less `LocalDateTime`. The UI intentionally uses a stable localized wall-clock timestamp instead of calculating relative time with an unsafe device/server timezone assumption.

## 7. Permission handling

`VIEW_AUDIT_LOGS` was added to centralized `PermissionCodes` and evaluated through `PermissionEvaluator`.

- Recent Activity: exact permission required.
- Notification badge: authenticated user only; no role or invented permission check.
- No role-name checks such as `SUPER_ADMIN` or `ACCOUNTANT` were introduced.
- Backend null filtering remains authoritative.

## 8. Failure isolation

Dashboard and unread count use independent repositories and independent in-flight flags within the existing single `DashboardViewModel`.

- Initial load requests both independently.
- Pull-to-refresh requests both independently.
- Unread-count loading/failure does not replace dashboard data, dashboard error, loading, refresh, or retry state.
- Dashboard success remains visible when the unread request fails.
- Unread success updates only the badge state and does not clear a dashboard fatal/nonfatal error.
- Existing unread count remains available while the dashboard refreshes.
- A badge error is held as separate nonfatal state and is not surfaced as a fatal dashboard error.
- The existing authenticated `RetrofitClient` supplies bearer tokens and refresh behavior to `NotificationApi`.

## 9. Tests added

Focused tests cover:

- Parsing the exact scalar `ApiResponse<Long>` unread-count response.
- `success: false` envelope handling.
- Null unread-count data handling.
- Badge boundaries: `0`, `1`, `99`, and `100`.
- Dashboard content surviving unread-count failure.
- Unread-count success preserving a dashboard failure.
- Recent Activity hidden/empty without `VIEW_AUDIT_LOGS`.
- Recent Activity limited to five.
- Null and empty recent activity.
- Safe readable fallback for unknown and missing action/entity values.

The complete local unit suite contains 14 tests.

## 10. Unit-test result

Command:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Result: **BUILD SUCCESSFUL** — 14 tests, 0 failures.

Gradle required network/cache access to obtain its configured distribution in the execution environment. The final validation completed successfully.

## 11. Build result

Command:

```powershell
.\gradlew.bat :app:assembleDebug
```

Result: **BUILD SUCCESSFUL**.

Debug APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 12. Manual emulator tests required

1. Sign in as a user without unread notifications: bell is accessible and badge is hidden.
2. Sign in as a user with 1, 99, and more than 99 unread notifications: badge text and content descriptions are correct.
3. Tap the bell and confirm the “coming next” Snackbar; confirm no navigation occurs.
4. Pull to refresh and verify dashboard content and badge both update.
5. Leave and return to the fragment within 60 seconds and confirm no redundant request; return after the interval and confirm refresh.
6. Simulate unread-count HTTP/network failure and confirm dashboard content remains visible and usable.
7. Simulate dashboard failure with a successful count and confirm the dashboard error remains visible.
8. Sign in without `VIEW_AUDIT_LOGS`: Recent Activity is completely absent.
9. Sign in with `VIEW_AUDIT_LOGS` and no logs: `No recent activity` appears.
10. Sign in with more than five audit entries: only the newest five supplied summary items render.
11. Verify null actor, null entity ID, null timestamp, and unknown action/entity values render without crashes.
12. Check light/dark theme badge contrast, touch target, TalkBack description, small screen scrolling, and large-font layout.

## 13. Deferred Phase 2 work

Explicitly deferred:

- Notification list screen and paging models
- Mark-one notification as read
- Mark-all notifications as read
- Notification navigation/routes
- Push notifications or polling
- Full audit timeline and “View all”
- Revenue/expense charts
- Budget health
- Bottom navigation
- Reports
- Backend or Angular changes
