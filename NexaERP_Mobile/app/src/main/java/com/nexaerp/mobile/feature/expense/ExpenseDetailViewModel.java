package com.nexaerp.mobile.feature.expense;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nexaerp.mobile.data.repository.ExpenseRepository;

public class ExpenseDetailViewModel extends ViewModel {
    private final ExpenseRepository repository;
    private final long expenseId;
    private final MutableLiveData<ExpenseDetailUiState> state =
            new MutableLiveData<>(ExpenseDetailUiState.loading());
    private boolean changed;

    public ExpenseDetailViewModel(ExpenseRepository repository, long expenseId) {
        this.repository = repository;
        this.expenseId = expenseId;
    }

    public LiveData<ExpenseDetailUiState> getState() {
        return state;
    }

    public boolean hasChanges() {
        return changed;
    }

    public void load() {
        state.setValue(ExpenseDetailUiState.loading());
        repository.loadExpense(expenseId, result -> {
            if (result.isSuccess()) {
                state.setValue(ExpenseDetailUiState.loading().withExpense(result.getItem()));
            } else {
                state.setValue(ExpenseDetailUiState.loading().withError(result.getErrorMessage()));
            }
        });
    }

    public void post() {
        ExpenseDetailUiState current = state.getValue();
        if (current == null || current.getExpense() == null || current.isActionInProgress()) return;
        state.setValue(current.withActionInProgress(true));
        repository.postExpense(expenseId, result -> {
            ExpenseDetailUiState latest = state.getValue();
            if (latest == null) return;
            if (result.isSuccess()) {
                changed = true;
                state.setValue(latest.withActionInProgress(false).withExpense(result.getItem()));
            } else {
                state.setValue(latest.withActionInProgress(false).withTransientError(
                        result.getErrorMessage()
                ));
            }
        });
    }

    public void cancel(String reason) {
        ExpenseDetailUiState current = state.getValue();
        if (current == null || current.getExpense() == null || current.isActionInProgress()) return;
        state.setValue(current.withActionInProgress(true));
        repository.cancelExpense(expenseId, reason, result -> {
            ExpenseDetailUiState latest = state.getValue();
            if (latest == null) return;
            if (result.isSuccess()) {
                changed = true;
                state.setValue(latest.withActionInProgress(false).withExpense(result.getItem()));
            } else {
                state.setValue(latest.withActionInProgress(false).withTransientError(
                        result.getErrorMessage()
                ));
            }
        });
    }
}