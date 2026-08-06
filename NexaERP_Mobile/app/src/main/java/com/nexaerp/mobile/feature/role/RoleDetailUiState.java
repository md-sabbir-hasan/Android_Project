package com.nexaerp.mobile.feature.role;

import com.nexaerp.mobile.data.remote.model.role.RoleResponse;

public final class RoleDetailUiState {
    private final boolean loading;
    private final RoleResponse role;
    private final String errorMessage;

    private RoleDetailUiState(boolean loading, RoleResponse role, String errorMessage) {
        this.loading = loading;
        this.role = role;
        this.errorMessage = errorMessage;
    }

    public static RoleDetailUiState loading() {
        return new RoleDetailUiState(true, null, null);
    }

    public RoleDetailUiState withRole(RoleResponse role) {
        return new RoleDetailUiState(false, role, null);
    }

    public RoleDetailUiState withError(String message) {
        return new RoleDetailUiState(false, null, message);
    }

    public boolean isLoading() { return loading; }
    public RoleResponse getRole() { return role; }
    public String getErrorMessage() { return errorMessage; }
}