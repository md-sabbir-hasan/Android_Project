package com.nexaerp.mobile.feature.user;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.nexaerp.mobile.data.repository.UserRepository;

public final class UserDetailViewModelFactory implements ViewModelProvider.Factory {
    private final UserRepository userRepository;
    private final long userId;

    public UserDetailViewModelFactory(UserRepository userRepository, long userId) {
        this.userRepository = userRepository;
        this.userId = userId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(UserDetailViewModel.class)) {
            return (T) new UserDetailViewModel(userRepository, userId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}