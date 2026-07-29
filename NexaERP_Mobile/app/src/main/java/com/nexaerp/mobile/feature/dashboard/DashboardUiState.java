package com.nexaerp.mobile.feature.dashboard;

import com.nexaerp.mobile.data.remote.model.dashboard.DashboardSummaryResponse;

public final class DashboardUiState {
    private final boolean loading;
    private final boolean refreshing;
    private final DashboardSummaryResponse data;
    private final String errorMessage;
    private final boolean retryable;

    private DashboardUiState(
            boolean loading,
            boolean refreshing,
            DashboardSummaryResponse data,
            String errorMessage,
            boolean retryable
    ) {
        this.loading = loading;
        this.refreshing = refreshing;
        this.data = data;
        this.errorMessage = errorMessage;
        this.retryable = retryable;
    }

    public static DashboardUiState initialLoading() {
        return new DashboardUiState(true, false, null, null, false);
    }

    public static DashboardUiState content(DashboardSummaryResponse data) {
        return new DashboardUiState(false, false, data, null, false);
    }

    public static DashboardUiState contentWithError(
            DashboardSummaryResponse data,
            String errorMessage,
            boolean retryable
    ) {
        return new DashboardUiState(false, false, data, errorMessage, retryable);
    }

    public static DashboardUiState refreshing(DashboardSummaryResponse data) {
        return new DashboardUiState(false, true, data, null, false);
    }

    public static DashboardUiState fatalError(String message, boolean retryable) {
        return new DashboardUiState(false, false, null, message, retryable);
    }

    public boolean isLoading() { return loading; }
    public boolean isRefreshing() { return refreshing; }
    public DashboardSummaryResponse getData() { return data; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isRetryable() { return retryable; }

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
