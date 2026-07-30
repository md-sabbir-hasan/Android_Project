package com.nexaerp.mobile.feature.dashboard;

import com.nexaerp.mobile.data.remote.model.dashboard.DashboardSummaryResponse;

public final class DashboardUiState {
    private final boolean loading;
    private final boolean refreshing;
    private final DashboardSummaryResponse data;
    private final String errorMessage;
    private final boolean retryable;
    private final Long unreadCount;
    private final boolean unreadCountLoading;
    private final String unreadCountError;

    private DashboardUiState(
            boolean loading,
            boolean refreshing,
            DashboardSummaryResponse data,
            String errorMessage,
            boolean retryable,
            Long unreadCount,
            boolean unreadCountLoading,
            String unreadCountError
    ) {
        this.loading = loading;
        this.refreshing = refreshing;
        this.data = data;
        this.errorMessage = errorMessage;
        this.retryable = retryable;
        this.unreadCount = unreadCount;
        this.unreadCountLoading = unreadCountLoading;
        this.unreadCountError = unreadCountError;
    }

    public static DashboardUiState initialLoading() {
        return new DashboardUiState(true, false, null, null, false, null, false, null);
    }

    public static DashboardUiState content(DashboardSummaryResponse data) {
        return new DashboardUiState(false, false, data, null, false, null, false, null);
    }

    public static DashboardUiState contentWithError(
            DashboardSummaryResponse data,
            String errorMessage,
            boolean retryable
    ) {
        return new DashboardUiState(false, false, data, errorMessage, retryable, null, false, null);
    }

    public static DashboardUiState refreshing(DashboardSummaryResponse data) {
        return new DashboardUiState(false, true, data, null, false, null, false, null);
    }

    public static DashboardUiState fatalError(String message, boolean retryable) {
        return new DashboardUiState(false, false, null, message, retryable, null, false, null);
    }

    public DashboardUiState withUnreadLoading() {
        return new DashboardUiState(
                loading, refreshing, data, errorMessage, retryable,
                unreadCount, true, null
        );
    }

    public DashboardUiState withUnreadCount(long count) {
        return new DashboardUiState(
                loading, refreshing, data, errorMessage, retryable,
                Math.max(0L, count), false, null
        );
    }

    public DashboardUiState withUnreadError(String message) {
        return new DashboardUiState(
                loading, refreshing, data, errorMessage, retryable,
                unreadCount, false, message
        );
    }

    public DashboardUiState preservingUnreadFrom(DashboardUiState previous) {
        if (previous == null) return this;
        return new DashboardUiState(
                loading, refreshing, data, errorMessage, retryable,
                previous.unreadCount, previous.unreadCountLoading, previous.unreadCountError
        );
    }

    public boolean isLoading() { return loading; }
    public boolean isRefreshing() { return refreshing; }
    public DashboardSummaryResponse getData() { return data; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isRetryable() { return retryable; }
    public Long getUnreadCount() { return unreadCount; }
    public boolean isUnreadCountLoading() { return unreadCountLoading; }
    public String getUnreadCountError() { return unreadCountError; }

    public boolean isEmptyOrAccessLimited() {
        return data != null
                && data.getUsers() == null
                && data.getSecurity() == null
                && data.getFinance() == null
                && data.getBusiness() == null
                && data.getSystem() == null
                && data.getRecentActivities() == null
                && data.getBudget() == null
                && data.getExpense() == null;
    }
}
