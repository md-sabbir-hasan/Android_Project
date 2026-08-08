package com.nexaerp.mobile.feature.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nexaerp.mobile.data.remote.model.role.RoleResponse;
import com.nexaerp.mobile.data.remote.model.user.UserRequest;
import com.nexaerp.mobile.data.remote.model.user.UserResponse;
import com.nexaerp.mobile.data.repository.RoleRepository;
import com.nexaerp.mobile.data.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

public class UserEditViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final long userId;
    private final MutableLiveData<UserEditUiState> state =
            new MutableLiveData<>(UserEditUiState.initial());

    public UserEditViewModel(UserRepository userRepository, RoleRepository roleRepository, long userId) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userId = userId;
    }

    public LiveData<UserEditUiState> getState() {
        return state;
    }

    public boolean isEditMode() {
        return userId > 0;
    }

    public void load() {
        roleRepository.loadRoles(roleResult -> {
            if (!roleResult.isSuccess()) {
                UserEditUiState current = state.getValue();
                if (current != null) state.setValue(current.withLoadError(roleResult.getErrorMessage()));
                return;
            }

            if (!isEditMode()) {
                UserEditUiState current = state.getValue();
                if (current != null) {
                    state.setValue(current.withLoaded(roleResult.getItems(), new HashSet<>(), "", ""));
                }
                return;
            }

            userRepository.loadUser(userId, userResult -> {
                UserEditUiState current = state.getValue();
                if (current == null) return;
                if (!userResult.isSuccess()) {
                    state.setValue(current.withLoadError(userResult.getErrorMessage()));
                    return;
                }
                UserResponse user = userResult.getItem();
                Set<Long> checkedIds = new HashSet<>();
                if (user.getRoles() != null) {
                    for (RoleResponse role : roleResult.getItems()) {
                        if (role.getId() != null && role.getName() != null
                                && user.getRoles().contains(role.getName())) {
                            checkedIds.add(role.getId());
                        }
                    }
                }
                state.setValue(current.withLoaded(
                        roleResult.getItems(),
                        checkedIds,
                        user.getName() == null ? "" : user.getName(),
                        user.getEmail() == null ? "" : user.getEmail()
                ));
            });
        });
    }

    public void setFields(String name, String email) {
        UserEditUiState current = state.getValue();
        if (current != null) state.setValue(current.withField(name, email));
    }

    public void toggleRole(long roleId, boolean checked) {
        UserEditUiState current = state.getValue();
        if (current != null) state.setValue(current.withToggleRole(roleId, checked));
    }

    public void save() {
        UserEditUiState current = state.getValue();
        if (current == null || current.isSaving()) return;

        if (current.getName() == null || current.getName().trim().isEmpty()) {
            state.setValue(current.withSaveError("Name is required."));
            return;
        }
        if (current.getEmail() == null || current.getEmail().trim().isEmpty()) {
            state.setValue(current.withSaveError("Email is required."));
            return;
        }
        if (current.getCheckedRoleIds().isEmpty()) {
            state.setValue(current.withSaveError("Select at least one role."));
            return;
        }

        state.setValue(current.withSaving());
        UserRequest request = new UserRequest(
                current.getName().trim(),
                current.getEmail().trim(),
                current.getCheckedRoleIds()
        );

        UserRepository.ItemResultCallback callback = result -> {
            UserEditUiState latest = state.getValue();
            if (latest == null) return;
            if (result.isSuccess()) {
                state.setValue(latest.withSaved());
            } else {
                state.setValue(latest.withSaveError(result.getErrorMessage()));
            }
        };

        if (isEditMode()) {
            userRepository.updateUser(userId, request, callback);
        } else {
            userRepository.createUser(request, callback);
        }
    }
}