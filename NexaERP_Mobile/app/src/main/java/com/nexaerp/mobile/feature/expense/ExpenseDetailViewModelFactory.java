package com.nexaerp.mobile.feature.expense;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.nexaerp.mobile.data.repository.ExpenseRepository;

public final class ExpenseDetailViewModelFactory implements ViewModelProvider.Factory {
    private final ExpenseRepository expenseRepository;
    private final long expenseId;

    public ExpenseDetailViewModelFactory(ExpenseRepository expenseRepository, long expenseId) {
        this.expenseRepository = expenseRepository;
        this.expenseId = expenseId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ExpenseDetailViewModel.class)) {
            return (T) new ExpenseDetailViewModel(expenseRepository, expenseId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}