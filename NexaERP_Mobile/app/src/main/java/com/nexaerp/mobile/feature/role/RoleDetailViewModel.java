package com.nexaerp.mobile.feature.role;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nexaerp.mobile.data.repository.RoleRepository;

public class RoleDetailViewModel extends ViewModel {
    private final RoleRepository repository;
    private final long roleId;
    private final MutableLiveData<RoleDetailUiState> state =
            new MutableLiveData<>(RoleDetailUiState.loading());

    public RoleDetailViewModel(RoleRepository repository, long roleId) {
        this.repository = repository;
        this.roleId = roleId;
    }

    public LiveData<RoleDetailUiState> getState() {
        return state;
    }

    public void load() {
        state.setValue(RoleDetailUiState.loading());
        repository.loadRole(roleId, result -> {
            if (result.isSuccess()) {
                state.setValue(RoleDetailUiState.loading().withRole(result.getItem()));
            } else {
                state.setValue(RoleDetailUiState.loading().withError(result.getErrorMessage()));
            }
        });
    }
}