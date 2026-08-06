package com.nexaerp.mobile.feature.role;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nexaerp.mobile.data.repository.RoleRepository;

public class RoleListViewModel extends ViewModel {
    private final RoleRepository repository;
    private final MutableLiveData<RoleListUiState> state =
            new MutableLiveData<>(RoleListUiState.initial());
    private boolean requestInFlight;

    public RoleListViewModel(RoleRepository repository) {
        this.repository = repository;
    }

    public LiveData<RoleListUiState> getState() {
        return state;
    }

    public void load() {
        if (requestInFlight) return;
        RoleListUiState current = state.getValue();
        if (current != null) state.setValue(current.withLoading());
        fetch();
    }

    public void refresh() {
        if (requestInFlight) return;
        RoleListUiState current = state.getValue();
        if (current != null) state.setValue(current.withRefreshing());
        fetch();
    }

    public void setQuery(String query) {
        RoleListUiState current = state.getValue();
        if (current != null) state.setValue(current.withQuery(query));
    }

    private void fetch() {
        requestInFlight = true;
        repository.loadRoles(result -> {
            requestInFlight = false;
            RoleListUiState current = state.getValue();
            if (current == null) return;
            if (result.isSuccess()) {
                state.setValue(current.withRoles(result.getItems()));
            } else {
                state.setValue(current.withError(result.getErrorMessage()));
            }
        });
    }
}