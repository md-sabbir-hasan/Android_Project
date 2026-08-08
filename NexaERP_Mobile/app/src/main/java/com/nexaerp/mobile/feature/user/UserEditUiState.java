package com.nexaerp.mobile.feature.user;

import com.nexaerp.mobile.data.remote.model.role.RoleResponse;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class UserEditUiState {
    private final boolean loading;
    private final boolean saving;
    private final List<RoleResponse> roles;
    private final Set<Long> checkedRoleIds;
    private final String name;
    private final String email;
    private final String errorMessage;
    private final boolean saved;

    private UserEditUiState(
            boolean loading,
            boolean saving,
            List<RoleResponse> roles,
            Set<Long> checkedRoleIds,
            String name,
            String email,
            String errorMessage,
            boolean saved
    ) {
        this.loading = loading;
        this.saving = saving;
        this.roles = roles;
        this.checkedRoleIds = checkedRoleIds;
        this.name = name;
        this.email = email;
        this.errorMessage = errorMessage;
        this.saved = saved;
    }

    public static UserEditUiState initial() {
        return new UserEditUiState(
                true, false, Collections.emptyList(), new HashSet<>(), "", "", null, false
        );
    }

    public UserEditUiState withLoaded(
            List<RoleResponse> roles, Set<Long> checkedIds, String name, String email
    ) {
        return new UserEditUiState(false, false, roles, checkedIds, name, email, null, false);
    }

    public UserEditUiState withLoadError(String message) {
        return new UserEditUiState(false, false, roles, checkedRoleIds, name, email, message, false);
    }

    public UserEditUiState withField(String newName, String newEmail) {
        return new UserEditUiState(loading, saving, roles, checkedRoleIds, newName, newEmail, null, false);
    }

    public UserEditUiState withToggleRole(long roleId, boolean checked) {
        Set<Long> updated = new HashSet<>(checkedRoleIds);
        if (checked) updated.add(roleId); else updated.remove(roleId);
        return new UserEditUiState(loading, saving, roles, updated, name, email, null, false);
    }

    public UserEditUiState withSaving() {
        return new UserEditUiState(loading, true, roles, checkedRoleIds, name, email, null, false);
    }

    public UserEditUiState withSaved() {
        return new UserEditUiState(loading, false, roles, checkedRoleIds, name, email, null, true);
    }

    public UserEditUiState withSaveError(String message) {
        return new UserEditUiState(loading, false, roles, checkedRoleIds, name, email, message, false);
    }

    public boolean isLoading() { return loading; }
    public boolean isSaving() { return saving; }
    public List<RoleResponse> getRoles() { return roles; }
    public Set<Long> getCheckedRoleIds() { return checkedRoleIds; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isSaved() { return saved; }
}