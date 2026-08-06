package com.nexaerp.mobile.feature.role;

import com.nexaerp.mobile.data.remote.model.role.RoleResponse;

import java.util.Collections;
import java.util.List;

public final class RoleListUiState {
    private final List<RoleResponse> allRoles;
    private final String query;
    private final boolean loading;
    private final boolean refreshing;
    private final String errorMessage;

    private RoleListUiState(
            List<RoleResponse> allRoles,
            String query,
            boolean loading,
            boolean refreshing,
            String errorMessage
    ) {
        this.allRoles = allRoles;
        this.query = query;
        this.loading = loading;
        this.refreshing = refreshing;
        this.errorMessage = errorMessage;
    }

    public static RoleListUiState initial() {
        return new RoleListUiState(Collections.emptyList(), "", true, false, null);
    }

    public RoleListUiState withLoading() {
        return new RoleListUiState(allRoles, query, true, false, null);
    }

    public RoleListUiState withRefreshing() {
        return new RoleListUiState(allRoles, query, false, true, null);
    }

    public RoleListUiState withRoles(List<RoleResponse> roles) {
        return new RoleListUiState(
                roles == null ? Collections.emptyList() : roles,
                query, false, false, null
        );
    }

    public RoleListUiState withError(String message) {
        return new RoleListUiState(allRoles, query, false, false, message);
    }

    public RoleListUiState withQuery(String newQuery) {
        return new RoleListUiState(allRoles, newQuery == null ? "" : newQuery, loading, refreshing, errorMessage);
    }

    public List<RoleResponse> getFilteredRoles() {
        if (query == null || query.trim().isEmpty()) {
            return allRoles;
        }
        String needle = query.trim().toLowerCase(java.util.Locale.getDefault());
        List<RoleResponse> result = new java.util.ArrayList<>();
        for (RoleResponse role : allRoles) {
            String name = role.getName() == null ? "" : role.getName().toLowerCase(java.util.Locale.getDefault());
            if (name.contains(needle)) {
                result.add(role);
            }
        }
        return result;
    }

    public boolean isLoading() { return loading; }
    public boolean isRefreshing() { return refreshing; }
    public String getErrorMessage() { return errorMessage; }
    public String getQuery() { return query; }
    public boolean isEmpty() {
        return !loading && errorMessage == null && getFilteredRoles().isEmpty();
    }
}