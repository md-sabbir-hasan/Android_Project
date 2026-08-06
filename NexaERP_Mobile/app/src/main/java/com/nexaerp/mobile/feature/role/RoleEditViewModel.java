package com.nexaerp.mobile.feature.role;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nexaerp.mobile.data.remote.model.role.PermissionResponse;
import com.nexaerp.mobile.data.remote.model.role.RoleRequest;
import com.nexaerp.mobile.data.remote.model.role.RoleResponse;
import com.nexaerp.mobile.data.repository.RoleRepository;

import java.util.HashSet;
import java.util.Set;

public class RoleEditViewModel extends ViewModel {
    private final RoleRepository repository;
    private final long roleId;
    private final MutableLiveData<RoleEditUiState> state =
            new MutableLiveData<>(RoleEditUiState.initial());

    public RoleEditViewModel(RoleRepository repository, long roleId) {
        this.repository = repository;
        this.roleId = roleId;
    }

    public LiveData<RoleEditUiState> getState() {
        return state;
    }

    public boolean isEditMode() {
        return roleId > 0;
    }

    public void load() {
        repository.loadPermissions(permissionResult -> {
            if (!permissionResult.isSuccess()) {
                RoleEditUiState current = state.getValue();
                if (current != null) {
                    state.setValue(current.withLoadError(permissionResult.getErrorMessage()));
                }
                return;
            }

            if (!isEditMode()) {
                RoleEditUiState current = state.getValue();
                if (current != null) {
                    state.setValue(current.withLoaded(
                            permissionResult.getItems(), new HashSet<>(), "", ""
                    ));
                }
                return;
            }

            repository.loadRole(roleId, roleResult -> {
                RoleEditUiState current = state.getValue();
                if (current == null) return;
                if (!roleResult.isSuccess()) {
                    state.setValue(current.withLoadError(roleResult.getErrorMessage()));
                    return;
                }
                RoleResponse role = roleResult.getItem();
                Set<Long> checkedIds = new HashSet<>();
                if (role.getPermissions() != null) {
                    for (RoleResponse.PermissionSummary permission : role.getPermissions()) {
                        if (permission.getId() != null) checkedIds.add(permission.getId());
                    }
                }
                state.setValue(current.withLoaded(
                        permissionResult.getItems(),
                        checkedIds,
                        role.getName() == null ? "" : role.getName(),
                        role.getDescription() == null ? "" : role.getDescription()
                ));
            });
        });
    }

    public void setFields(String name, String description) {
        RoleEditUiState current = state.getValue();
        if (current != null) state.setValue(current.withField(name, description));
    }

    public void togglePermission(long permissionId, boolean checked) {
        RoleEditUiState current = state.getValue();
        if (current != null) state.setValue(current.withTogglePermission(permissionId, checked));
    }

    public void save() {
        RoleEditUiState current = state.getValue();
        if (current == null || current.isSaving()) return;

        if (current.getName() == null || current.getName().trim().isEmpty()) {
            state.setValue(current.withSaveError("Role name is required."));
            return;
        }

        state.setValue(current.withSaving());
        RoleRequest request = new RoleRequest(
                current.getName().trim(),
                current.getDescription() == null ? null : current.getDescription().trim(),
                current.getCheckedPermissionIds()
        );

        RoleRepository.ItemResultCallback callback = result -> {
            RoleEditUiState latest = state.getValue();
            if (latest == null) return;
            if (result.isSuccess()) {
                state.setValue(latest.withSaved());
            } else {
                state.setValue(latest.withSaveError(result.getErrorMessage()));
            }
        };

        if (isEditMode()) {
            repository.updateRole(roleId, request, callback);
        } else {
            repository.createRole(request, callback);
        }
    }
}