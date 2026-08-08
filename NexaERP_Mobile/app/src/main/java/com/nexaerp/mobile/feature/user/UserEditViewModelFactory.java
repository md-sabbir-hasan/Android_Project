package com.nexaerp.mobile.feature.user;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.nexaerp.mobile.data.repository.RoleRepository;
import com.nexaerp.mobile.data.repository.UserRepository;

public final class UserEditViewModelFactory implements ViewModelProvider.Factory {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final long userId;

    public UserEditViewModelFactory(
            UserRepository userRepository, RoleRepository roleRepository, long userId
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userId = userId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(UserEditViewModel.class)) {
            return (T) new UserEditViewModel(userRepository, roleRepository, userId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}