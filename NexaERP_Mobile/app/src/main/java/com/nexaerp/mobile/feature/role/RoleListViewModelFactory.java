package com.nexaerp.mobile.feature.role;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.nexaerp.mobile.data.repository.RoleRepository;

public final class RoleListViewModelFactory implements ViewModelProvider.Factory {
    private final RoleRepository roleRepository;

    public RoleListViewModelFactory(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RoleListViewModel.class)) {
            return (T) new RoleListViewModel(roleRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}