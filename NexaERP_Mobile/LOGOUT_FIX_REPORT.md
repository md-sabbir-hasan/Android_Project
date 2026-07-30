# Android Logout Fix Report

Date: 2026-07-30  
Scope: `NexaERP_Mobile` only  
Backend changes: none

## 1. Root cause

Logout logic itself was still valid in `MainActivity`: it captured the access token, enqueued a best-effort backend logout, synchronously cleared `TokenManager`, and opened `LoginActivity` with `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK`.

The UI ownership was the defect:

- The only logout control existed in `activity_main.xml` as a child of the Activity toolbar.
- Its listener existed only in `MainActivity`.
- The Phase 2A visible interaction header is rendered by `DashboardFragment`.
- `fragment_dashboard.xml` had notification controls but no logout control.
- `DashboardFragment` had no way to request Activity-owned logout.

The dashboard fragment is constrained below the Activity toolbar, so its content did not literally overlay the toolbar in XML. Login and Splash also contain no logic that redirects a cleared session back to `MainActivity`. `SessionManager` only dispatches explicit session-expiry notifications. The failure was therefore not caused by token clearing, task flags, Login/Splash redirection, or an unexpected lifecycle redirect; it was the missing visible Fragment-owned entry point and communication path.

The fix places the single logout button in the dashboard header and delegates the action through a small `DashboardFragment.LogoutCallback` implemented by `MainActivity`.

## 2. Exact files modified

- `app/src/main/java/com/nexaerp/mobile/MainActivity.java`
- `app/src/main/java/com/nexaerp/mobile/feature/dashboard/DashboardFragment.java`
- `app/src/main/res/layout/activity_main.xml`
- `app/src/main/res/layout/fragment_dashboard.xml`
- `app/src/main/res/values/strings.xml`

Created:

- `app/src/main/res/drawable/ic_logout.xml`
- `LOGOUT_FIX_REPORT.md`

No backend, authentication client, interceptor, authenticator, refresh, session manager, token manager, LoginActivity, SplashActivity, manifest, notification, or recent-activity implementation was changed.

## 3. Exact logout flow after the fix

1. The user taps the logout icon in the visible dashboard header.
2. `DashboardFragment.requestLogout()`:
   - checks that ViewBinding and the host callback are still valid;
   - ignores a repeat tap once the button is disabled;
   - disables the logout button;
   - invokes `LogoutCallback.onLogoutRequested()`.
3. `MainActivity.onLogoutRequested()`:
   - returns immediately if logout is already in progress;
   - sets `logoutInProgress`;
   - cancels and clears any pending `/api/auth/me` call;
   - reads the access token before clearing local state;
   - if a token exists, enqueues best-effort `POST /api/auth/logout`;
   - does not wait for, inspect, or depend on that response;
   - synchronously calls `TokenManager.clearSession()`;
   - starts `LoginActivity` with `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK`.
4. Android clears the authenticated task. Back cannot reopen `MainActivity`.

`TokenManager.clearSession()` uses synchronous `SharedPreferences.commit()` after `.clear()`, removing:

- access token;
- refresh token;
- access-token lifetime;
- calculated expiry;
- user ID;
- saved name;
- saved email.

Backend network failure, HTTP 401, HTTP 500, null response, or delayed response cannot block local logout/navigation.

## 4. Duplicate button/layout removed

The old text logout button was removed from `activity_main.xml`. There is now one logout control only: the icon button in the `DashboardFragment` header beside the notification control.

The Activity toolbar and its title remain unchanged otherwise. The dashboard was not redesigned.

The new icon has a 48 dp touch target and the content description `Log out`.

## 5. Lifecycle, pending-call, and duplicate-event safety

- Fragment host communication uses an interface assigned in `onAttach()` and cleared in `onDetach()`.
- The click path checks the current binding and callback before use.
- The button is disabled before invoking the Activity.
- `MainActivity.logoutInProgress` prevents duplicate backend calls and navigation from repeated taps.
- The same guard prevents a late `SessionManager` expiry event from launching Login a second time during explicit logout.
- The pending current-user call is cancelled during logout.
- Its response/failure callbacks now ignore cancelled calls and ignore all results while logout is in progress, preventing a late callback from changing Activity UI.
- `DashboardFragment` continues clearing its binding in `onDestroyView()`.
- Session-expiry logout still clears local state and uses the same task-clearing Login navigation.

## 6. Test result

Command:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Result: **BUILD SUCCESSFUL**.

The existing 14 unit tests passed with no failures. No JVM-only test was added for the Fragment-to-Activity lifecycle gesture because that behavior requires an instrumented Activity/Fragment environment; it is covered by the manual checklist below.

## 7. Build result

Command:

```powershell
.\gradlew.bat :app:assembleDebug
```

Result: **BUILD SUCCESSFUL**.

Debug APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 8. Preserved behavior

The fix does not alter:

- login;
- token refresh;
- bearer-token interception;
- authenticator behavior;
- session-expiry behavior;
- dashboard loading, refresh, retry, and errors;
- notification unread badge;
- Recent Activity permissions or rendering.

## 9. Manual emulator test checklist

1. Sign in and confirm one logout icon is visible and clickable in the dashboard header.
2. Confirm the notification icon/badge remains visible and functional beside it.
3. Confirm Recent Activity and all existing dashboard sections still render.
4. Tap logout once:
   - Login opens immediately;
   - access and refresh tokens, expiry, user ID, name, and email are absent from `nexa_auth`;
   - pressing Back cannot reopen Dashboard.
5. Rapidly tap logout twice and confirm only one navigation occurs and no crash appears.
6. Disable network before tapping logout and confirm Login still opens immediately.
7. Make backend logout return 401 and 500 in controlled testing; confirm local logout remains successful.
8. Start logout while `/api/auth/me` is pending and confirm no late loading/error UI appears.
9. Allow an access token to expire naturally and confirm `SessionManager` expiry still redirects to Login once.
10. Rotate/recreate the dashboard, then tap logout and confirm the Fragment callback and binding remain safe.
11. Use TalkBack and confirm the icon is announced as `Log out`.
12. Verify light/dark themes and small-screen layouts do not clip the logout or notification controls.
