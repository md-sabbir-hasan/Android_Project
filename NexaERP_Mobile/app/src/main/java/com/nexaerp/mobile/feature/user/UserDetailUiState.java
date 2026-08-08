package com.nexaerp.mobile.feature.user;

import com.nexaerp.mobile.data.remote.model.user.UserResponse;

public final class UserDetailUiState {
    private final boolean loading;
    private final UserResponse user;
    private final String errorMessage;
    private final boolean statusUpdating;
    private final String transientMessage;

    private UserDetailUiState(
            boolean loading,
            UserResponse user,
            String errorMessage,
            boolean statusUpdating,
            String transientMessage
    ) {
        this.loading = loading;
        this.user = user;
        this.errorMessage = errorMessage;
        this.statusUpdating = statusUpdating;
        this.transientMessage = transientMessage;
    }

    public static UserDetailUiState loading() {
        return new UserDetailUiState(true, null, null, false, null);
    }

    public UserDetailUiState withUser(UserResponse user) {
        return new UserDetailUiState(false, user, null, false, null);
    }

    public UserDetailUiState withError(String message) {
        return new UserDetailUiState(false, null, message, false, null);
    }

    public UserDetailUiState withStatusUpdating(boolean updating) {
        return new UserDetailUiState(loading, user, errorMessage, updating, null);
    }

    public UserDetailUiState withTransientError(String message) {
        return new UserDetailUiState(loading, user, errorMessage, false, message);
    }

    public boolean isLoading() { return loading; }
    public UserResponse getUser() { return user; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isStatusUpdating() { return statusUpdating; }
    public String getTransientMessage() { return transientMessage; }
}