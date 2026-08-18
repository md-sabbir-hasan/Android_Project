package com.nexaerp.mobile.feature.expense;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nexaerp.mobile.data.repository.ExpenseRepository;

public class ExpenseListViewModel extends ViewModel {
    private final ExpenseRepository repository;
    private final MutableLiveData<ExpenseListUiState> state =
            new MutableLiveData<>(ExpenseListUiState.initial());
    private boolean requestInFlight;

    public ExpenseListViewModel(ExpenseRepository repository) {
        this.repository = repository;
    }

    public LiveData<ExpenseListUiState> getState() {
        return state;
    }

    public void load() {
        if (requestInFlight) return;
        ExpenseListUiState current = state.getValue();
        if (current != null) state.setValue(current.withLoading());
        fetch();
    }

    public void refresh() {
        if (requestInFlight) return;
        ExpenseListUiState current = state.getValue();
        if (current != null) state.setValue(current.withRefreshing());
        fetch();
    }

    public void setStatusFilter(String status) {
        ExpenseListUiState current = state.getValue();
        if (current != null) state.setValue(current.withStatusFilter(status));
    }

    public void setQuery(String query) {
        ExpenseListUiState current = state.getValue();
        if (current != null) state.setValue(current.withQuery(query));
    }

    private void fetch() {
        requestInFlight = true;
        repository.loadExpenses(result -> {
            requestInFlight = false;
            ExpenseListUiState current = state.getValue();
            if (current == null) return;
            if (result.isSuccess()) {
                state.setValue(current.withExpenses(result.getItems()));
            } else {
                state.setValue(current.withError(result.getErrorMessage()));
            }
        });
    }
}