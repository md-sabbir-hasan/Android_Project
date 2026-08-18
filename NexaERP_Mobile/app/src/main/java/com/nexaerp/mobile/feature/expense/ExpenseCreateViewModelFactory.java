package com.nexaerp.mobile.feature.expense;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.nexaerp.mobile.data.repository.ExpenseRepository;

public final class ExpenseCreateViewModelFactory implements ViewModelProvider.Factory {
    private final ExpenseRepository expenseRepository;

    public ExpenseCreateViewModelFactory(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ExpenseCreateViewModel.class)) {
            return (T) new ExpenseCreateViewModel(expenseRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}