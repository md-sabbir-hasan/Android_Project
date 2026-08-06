package com.nexaerp.mobile.feature.role;

import com.nexaerp.mobile.data.remote.model.role.PermissionResponse;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RoleEditUiState {
    private final boolean loading;
    private final boolean saving;
    private final List<PermissionResponse> permissions;
    private final Set<Long> checkedPermissionIds;
    private final String name;
    private final String description;
    private final String errorMessage;
    private final boolean saved;

    private RoleEditUiState(
            boolean loading,
            boolean saving,
            List<PermissionResponse> permissions,
            Set<Long> checkedPermissionIds,
            String name,
            String description,
            String errorMessage,
            boolean saved
    ) {
        this.loading = loading;
        this.saving = saving;
        this.permissions = permissions;
        this.checkedPermissionIds = checkedPermissionIds;
        this.name = name;
        this.description = description;
        this.errorMessage = errorMessage;
        this.saved = saved;
    }

    public static RoleEditUiState initial() {
        return new RoleEditUiState(
                true, false, Collections.emptyList(), new HashSet<>(), "", "", null, false
        );
    }

    public RoleEditUiState withLoaded(
            List<PermissionResponse> permissions,
            Set<Long> checkedIds,
            String name,
            String description
    ) {
        return new RoleEditUiState(
                false, false, permissions, checkedIds, name, description, null, false
        );
    }

    public RoleEditUiState withLoadError(String message) {
        return new RoleEditUiState(
                false, false, permissions, checkedPermissionIds, name, description, message, false
        );
    }

    public RoleEditUiState withField(String newName, String newDescription) {
        return new RoleEditUiState(
                loading, saving, permissions, checkedPermissionIds,
                newName, newDescription, null, false
        );
    }

    public RoleEditUiState withTogglePermission(long permissionId, boolean checked) {
        Set<Long> updated = new HashSet<>(checkedPermissionIds);
        if (checked) updated.add(permissionId); else updated.remove(permissionId);
        return new RoleEditUiState(
                loading, saving, permissions, updated, name, description, null, false
        );
    }

    public RoleEditUiState withSaving() {
        return new RoleEditUiState(
                loading, true, permissions, checkedPermissionIds, name, description, null, false
        );
    }

    public RoleEditUiState withSaved() {
        return new RoleEditUiState(
                loading, false, permissions, checkedPermissionIds, name, description, null, true
        );
    }

    public RoleEditUiState withSaveError(String message) {
        return new RoleEditUiState(
                loading, false, permissions, checkedPermissionIds, name, description, message, false
        );
    }

    public boolean isLoading() { return loading; }
    public boolean isSaving() { return saving; }
    public List<PermissionResponse> getPermissions() { return permissions; }
    public Set<Long> getCheckedPermissionIds() { return checkedPermissionIds; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isSaved() { return saved; }
}