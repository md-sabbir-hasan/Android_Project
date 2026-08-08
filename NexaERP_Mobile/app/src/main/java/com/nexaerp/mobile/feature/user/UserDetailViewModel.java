package com.nexaerp.mobile.feature.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nexaerp.mobile.data.repository.UserRepository;

public class UserDetailViewModel extends ViewModel {
    private final UserRepository repository;
    private final long userId;
    private final MutableLiveData<UserDetailUiState> state =
            new MutableLiveData<>(UserDetailUiState.loading());
    private boolean changed;

    public UserDetailViewModel(UserRepository repository, long userId) {
        this.repository = repository;
        this.userId = userId;
    }

    public LiveData<UserDetailUiState> getState() {
        return state;
    }

    public boolean hasChanges() {
        return changed;
    }

    public void load() {
        state.setValue(UserDetailUiState.loading());
        repository.loadUser(userId, result -> {
            if (result.isSuccess()) {
                state.setValue(UserDetailUiState.loading().withUser(result.getItem()));
            } else {
                state.setValue(UserDetailUiState.loading().withError(result.getErrorMessage()));
            }
        });
    }

    public void toggleStatus() {
        UserDetailUiState current = state.getValue();
        if (current == null || current.getUser() == null || current.isStatusUpdating()) return;

        boolean isActive = "ACTIVE".equals(current.getUser().getStatus());
        state.setValue(current.withStatusUpdating(true));

        UserRepository.VoidResultCallback callback = result -> {
            UserDetailUiState latest = state.getValue();
            if (latest == null) return;
            if (result.isSuccess()) {
                changed = true;
                load();
            } else {
                state.setValue(latest.withStatusUpdating(false).withTransientError(
                        result.getErrorMessage()
                ));
            }
        };

        if (isActive) {
            repository.deactivateUser(userId, callback);
        } else {
            repository.activateUser(userId, callback);
        }
    }
}