package com.nexaerp.mobile.feature.expense;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.nexaerp.mobile.data.repository.ExpenseRepository;

public final class ExpenseListViewModelFactory implements ViewModelProvider.Factory {
    private final ExpenseRepository expenseRepository;

    public ExpenseListViewModelFactory(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ExpenseListViewModel.class)) {
            return (T) new ExpenseListViewModel(expenseRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}