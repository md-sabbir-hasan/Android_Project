# Dashboard Phase 1 Report

## 1. Backend dashboard endpoint inspected

- Method and path: `GET /api/dashboard/summary`
- Controller: `DashboardController`
- Authentication: `@PreAuthorize("isAuthenticated()")`
- Response: `ApiResponse<DashboardSummaryDto>`
- Successful message: `Dashboard summary loaded`
- Wrapper fields:
  - `success`: `boolean`
  - `message`: `String`
  - `data`: `DashboardSummaryDto`

No backend file was modified.

## 2. Exact DTOs and fields found

Android response names use `Response`, but fields and Java value types match the backend DTOs.

### DashboardSummaryResponse

- `users`: `UserSummaryResponse`
- `security`: `SecuritySummaryResponse`
- `finance`: `FinanceSummaryResponse`
- `business`: `BusinessSummaryResponse`
- `system`: `SystemSummaryResponse`
- `recentActivities`: `List<RecentActivityResponse>`
- `budget`: `BudgetDashboardResponse`
- `expense`: `ExpenseDashboardResponse`

### UserSummaryResponse

- `total`, `active`, `pending`, `inactive`, `locked`: `Long`

### SecuritySummaryResponse

- `totalRoles`, `totalPermissions`: `Long`

### FinanceSummaryResponse

- `totalAccounts`, `totalJournalEntries`, `postedJournalEntries`,
  `draftJournalEntries`, `reversedJournalEntries`: `Long`

### BusinessSummaryResponse

- `cashPosition`: `BigDecimal`
- `cashConfigured`: `Boolean`
- `asOfDate`: `LocalDate`
- `currencyCode`: `String`
- `accountsReceivable`: `BigDecimal`
- `overdueInvoiceCount`: `Long`
- `overdueInvoiceAmount`: `BigDecimal`
- `accountsPayable`: `BigDecimal`
- `overdueBillCount`: `Long`
- `overdueBillAmount`: `BigDecimal`
- `revenueTrend`, `expenseTrend`: `List<MonthlyTrendResponse>`
- `trendFromDate`, `trendToDate`: `LocalDate`

### MonthlyTrendResponse

- `month`: `String`
- `amount`: `BigDecimal`

### ExpenseDashboardResponse

- `draftCount`: `long`
- `draftTotalAmount`: `BigDecimal`
- `postedThisMonthTotal`: `BigDecimal`
- `recurringActiveCount`: `long`
- `recurringDueSoonCount`: `long`
- `outstandingDue`: `BigDecimal`

### RecentActivityResponse

- `action`, `entityName`, `userName`, `description`: `String`
- `entityId`: `Long`
- `createdAt`: `LocalDateTime`

### SystemSummaryResponse

- `applicationVersion`, `serverTimezone`, `environment`, `javaVersion`: `String`
- `serverTime`: `LocalDateTime`

### BudgetDashboardResponse

- `hasActiveBudget`: `boolean`
- `activeBudgetId`: `Long`
- `activeBudgetName`, `unavailableReason`, `currencyCode`: `String`
- `fromDate`, `toDate`: `LocalDate`
- `totalExpenseBudget`, `totalExpenseActualYtd`, `expenseUtilizationPercent`,
  `totalRevenueBudget`, `totalRevenueActualYtd`, `revenueAchievementPercent`:
  `BigDecimal`
- `topAccounts`: `List<BudgetTopAccountResponse>`

### BudgetTopAccountResponse

- `accountId`: `Long`
- `accountCode`, `accountName`: `String`
- `budgetAmount`, `actualAmount`, `utilizationPercent`: `BigDecimal`

Every DTO has a public no-argument constructor plus getters and setters. No
Lombok or invented fields were added. Explicit Gson adapters deserialize the
backend ISO `LocalDate` and `LocalDateTime` values.

## 3. Permission-dependent nullable sections

- `users`: requires `MANAGE_USERS`
- `security`: present with `MANAGE_ROLES` and/or `MANAGE_PERMISSIONS`;
  individual counts remain null without their corresponding permission
- `finance`: present with `VIEW_ACCOUNTS` and/or `VIEW_JOURNAL`; individual
  metrics remain null without their corresponding permission
- `business`: present with at least one of `VIEW_BANKING`, `VIEW_INVOICE`,
  `VIEW_VENDOR_BILL`, or `VIEW_REPORT`; only authorized subfields are populated
- `system`: requires `MANAGE_SETTINGS`
- `recentActivities`: requires `VIEW_AUDIT_LOGS`
- `budget`: requires `VIEW_BUDGET_REPORT`
- `expense`: requires `VIEW_EXPENSE`
- recurring expense metrics are populated only with `VIEW_RECURRING_EXPENSE`

The Android UI also treats backend nulls as authoritative and does not infer
visibility from role names.

## 4. Files created

### API, DTOs, and transport

- `data/remote/api/DashboardApi.java`
- `data/remote/client/LocalDateAdapter.java`
- `data/remote/client/LocalDateTimeAdapter.java`
- `data/remote/model/dashboard/DashboardSummaryResponse.java`
- `data/remote/model/dashboard/UserSummaryResponse.java`
- `data/remote/model/dashboard/SecuritySummaryResponse.java`
- `data/remote/model/dashboard/FinanceSummaryResponse.java`
- `data/remote/model/dashboard/BusinessSummaryResponse.java`
- `data/remote/model/dashboard/MonthlyTrendResponse.java`
- `data/remote/model/dashboard/ExpenseDashboardResponse.java`
- `data/remote/model/dashboard/RecentActivityResponse.java`
- `data/remote/model/dashboard/SystemSummaryResponse.java`
- `data/remote/model/dashboard/BudgetDashboardResponse.java`
- `data/remote/model/dashboard/BudgetTopAccountResponse.java`
- `data/repository/DashboardRepository.java`

### Core and dashboard feature

- `core/permission/PermissionCodes.java`
- `core/permission/PermissionEvaluator.java`
- `core/formatting/MoneyFormatter.java`
- `feature/dashboard/DashboardFragment.java`
- `feature/dashboard/DashboardViewModel.java`
- `feature/dashboard/DashboardViewModelFactory.java`
- `feature/dashboard/DashboardUiState.java`
- `feature/dashboard/QuickAction.java`
- `feature/dashboard/QuickActionProvider.java`
- `res/layout/fragment_dashboard.xml`

### Tests

- `DashboardDtoParsingTest.java`
- `DashboardPermissionAndStateTest.java`
- `DashboardRepositoryTest.java`

### Documentation

- `DASHBOARD_PHASE1_REPORT.md`

## 5. Files modified

- `data/remote/client/RetrofitClient.java`
- `MainActivity.java`
- `res/layout/activity_main.xml`
- `res/values/strings.xml`

## 6. Dashboard UI implemented

- Material 3 Java/XML dashboard fragment using ViewBinding
- Greeting with verified authenticated user name
- Readable joined role label
- Client-side last-updated time
- Horizontally scrollable, permission-filtered quick-action chips
- Financial overview using only backend values:
  - cash position
  - accounts receivable
  - accounts payable
  - posted expenses this month
- Backend currency code used when supplied
- `Cash accounts not configured` shown when `cashConfigured == false`
- Attention section for non-zero:
  - overdue invoice count
  - overdue vendor-bill count
  - draft expense count
  - recurring expenses due within seven days
- Unavailable metrics and zero-action attention sections are hidden

Charts, budgets, recent activity, administration, and system sections are
deserialized for contract correctness but are intentionally not rendered.

## 7. Quick-action permission behavior

- New Invoice: `CREATE_INVOICE`
- New Expense: `CREATE_EXPENSE`
- New Journal: `CREATE_JOURNAL`
- New Payment: `CREATE_PAYMENT`
- New Vendor Bill: `CREATE_VENDOR_BILL`

Actions are created only when the exact permission exists. Clicking any Phase 1
action displays `This feature is coming next.` No placeholder activities were
created.

## 8. Loading, refresh, error, and empty behavior

- Initial request displays a centered progress state.
- Pull-to-refresh retains existing dashboard data.
- Duplicate simultaneous requests are ignored.
- Successful API envelopes require non-null `data`.
- `success=false`, HTTP errors, network failures, null bodies, and null data are
  normalized by the repository.
- Fatal failures show a user-safe message and retry button.
- Refresh failures retain content and show a Snackbar.
- A response with all top-level permission-dependent sections null shows the
  access-limited empty message.

## 9. MainActivity integration

- `MainActivity` remains the authenticated destination.
- Existing `/api/auth/me` verification remains in place.
- `DashboardFragment` is attached only after a valid current user is returned.
- Verified name, roles, and permissions are passed to the fragment.
- Existing session-expiration listener, token clearing, login redirection, and
  logout behavior are preserved.
- No bottom navigation or additional destination fragments were added.

## 10. Tests added

- Exact JSON parsing for nested DTOs, `BigDecimal`, `LocalDate`, and
  `LocalDateTime`
- Nullable dashboard section parsing
- Exact permission-filtered quick actions
- Empty/access-limited state recognition
- Repository handling for backend `success=false`
- Repository handling for successful envelopes with null data

Result: `:app:testDebugUnitTest` passed.

## 11. Build result

- `.\gradlew.bat :app:testDebugUnitTest`: **BUILD SUCCESSFUL**
- `.\gradlew.bat :app:assembleDebug`: **BUILD SUCCESSFUL**
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

## 12. Manual tests still required

- Authenticate against a running backend and verify a full-permission dashboard.
- Verify users with partial and minimal permission sets.
- Verify each quick-action chip appears only for its exact create permission.
- Verify cash configured, cash unconfigured, and missing-currency cases.
- Verify non-zero and all-zero attention states.
- Verify pull-to-refresh with success and simulated network failure.
- Verify fatal failure/retry and expired-session redirection.
- Verify layout, scrolling, large font sizes, dark theme, and device rotation.
- Verify currency presentation for backend-supported currency codes/locales.

## 13. Deferred Phase 2 work

- Revenue/expense chart and chart dependency
- Notifications API, badge, and notification screen
- Budget detail UI
- Recent activity UI
- Administration and system summaries
- Bottom navigation and module destinations
- Dashboard disk cache or Room
- Dependency injection
- Compose
- Real quick-action destinations
- Notification/report/invoice/expense APIs beyond the dashboard summary
