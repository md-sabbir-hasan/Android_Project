package com.nexaerp.mobile.feature.role;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.nexaerp.mobile.data.repository.RoleRepository;

public final class RoleEditViewModelFactory implements ViewModelProvider.Factory {
    private final RoleRepository roleRepository;
    private final long roleId;

    public RoleEditViewModelFactory(RoleRepository roleRepository, long roleId) {
        this.roleRepository = roleRepository;
        this.roleId = roleId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RoleEditViewModel.class)) {
            return (T) new RoleEditViewModel(roleRepository, roleId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}