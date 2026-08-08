package com.nexaerp.mobile.feature.user;

import com.nexaerp.mobile.data.remote.model.user.UserResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class UserListUiState {
    private final List<UserResponse> items;
    private final String search;
    private final String statusFilter; // null = All
    private final boolean loading;
    private final boolean refreshing;
    private final boolean loadingMore;
    private final boolean hasMore;
    private final String errorMessage;
    private final String transientMessage;

    private UserListUiState(
            List<UserResponse> items,
            String search,
            String statusFilter,
            boolean loading,
            boolean refreshing,
            boolean loadingMore,
            boolean hasMore,
            String errorMessage,
            String transientMessage
    ) {
        this.items = items;
        this.search = search;
        this.statusFilter = statusFilter;
        this.loading = loading;
        this.refreshing = refreshing;
        this.loadingMore = loadingMore;
        this.hasMore = hasMore;
        this.errorMessage = errorMessage;
        this.transientMessage = transientMessage;
    }

    public static UserListUiState initial() {
        return new UserListUiState(Collections.emptyList(), "", null, true, false, false, true, null, null);
    }

    public UserListUiState withLoading() {
        return new UserListUiState(items, search, statusFilter, true, false, false, hasMore, null, null);
    }

    public UserListUiState withRefreshing() {
        return new UserListUiState(items, search, statusFilter, false, true, false, hasMore, null, null);
    }

    public UserListUiState withLoadingMore() {
        return new UserListUiState(items, search, statusFilter, false, false, true, hasMore, null, null);
    }

    public UserListUiState withPage(List<UserResponse> newItems, boolean last) {
        return new UserListUiState(newItems, search, statusFilter, false, false, false, !last, null, null);
    }

    public UserListUiState withAppendedPage(List<UserResponse> appended, boolean last) {
        List<UserResponse> merged = new ArrayList<>(items);
        merged.addAll(appended);
        return new UserListUiState(merged, search, statusFilter, false, false, false, !last, null, null);
    }

    public UserListUiState withFatalError(String message) {
        return new UserListUiState(
                Collections.emptyList(), search, statusFilter, false, false, false, false, message, null
        );
    }

    public UserListUiState withTransientError(String message) {
        return new UserListUiState(
                items, search, statusFilter, false, false, false, hasMore, null, message
        );
    }

    public UserListUiState withSearch(String newSearch) {
        return new UserListUiState(
                Collections.emptyList(), newSearch == null ? "" : newSearch,
                statusFilter, true, false, false, true, null, null
        );
    }

    public UserListUiState withStatusFilter(String newStatus) {
        return new UserListUiState(
                Collections.emptyList(), search, newStatus, true, false, false, true, null, null
        );
    }

    public List<UserResponse> getItems() { return items; }
    public String getSearch() { return search; }
    public String getStatusFilter() { return statusFilter; }
    public boolean isLoading() { return loading; }
    public boolean isRefreshing() { return refreshing; }
    public boolean isLoadingMore() { return loadingMore; }
    public boolean hasMore() { return hasMore; }
    public String getErrorMessage() { return errorMessage; }
    public String getTransientMessage() { return transientMessage; }
    public boolean isEmpty() {
        return !loading && errorMessage == null && items.isEmpty();
    }
}