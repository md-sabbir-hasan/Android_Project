package com.nexaerp.mobile.feature.expense;

import com.nexaerp.mobile.data.remote.model.expense.ExpenseResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class ExpenseListUiState {
    private final List<ExpenseResponse> allExpenses;
    private final String statusFilter; // null = All
    private final String query;
    private final boolean loading;
    private final boolean refreshing;
    private final String errorMessage;

    private ExpenseListUiState(
            List<ExpenseResponse> allExpenses,
            String statusFilter,
            String query,
            boolean loading,
            boolean refreshing,
            String errorMessage
    ) {
        this.allExpenses = allExpenses;
        this.statusFilter = statusFilter;
        this.query = query;
        this.loading = loading;
        this.refreshing = refreshing;
        this.errorMessage = errorMessage;
    }

    public static ExpenseListUiState initial() {
        return new ExpenseListUiState(Collections.emptyList(), null, "", true, false, null);
    }

    public ExpenseListUiState withLoading() {
        return new ExpenseListUiState(allExpenses, statusFilter, query, true, false, null);
    }

    public ExpenseListUiState withRefreshing() {
        return new ExpenseListUiState(allExpenses, statusFilter, query, false, true, null);
    }

    public ExpenseListUiState withExpenses(List<ExpenseResponse> expenses) {
        return new ExpenseListUiState(
                expenses == null ? Collections.emptyList() : expenses,
                statusFilter, query, false, false, null
        );
    }

    public ExpenseListUiState withError(String message) {
        return new ExpenseListUiState(Collections.emptyList(), statusFilter, query, false, false, message);
    }

    public ExpenseListUiState withStatusFilter(String status) {
        return new ExpenseListUiState(allExpenses, status, query, loading, refreshing, errorMessage);
    }

    public ExpenseListUiState withQuery(String newQuery) {
        return new ExpenseListUiState(
                allExpenses, statusFilter, newQuery == null ? "" : newQuery, loading, refreshing, errorMessage
        );
    }

    public List<ExpenseResponse> getFilteredExpenses() {
        List<ExpenseResponse> result = new ArrayList<>();
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.getDefault());
        for (ExpenseResponse expense : allExpenses) {
            if (statusFilter != null && !statusFilter.equals(expense.getStatus())) continue;
            if (!needle.isEmpty()) {
                String account = expense.getExpenseAccountName() == null
                        ? "" : expense.getExpenseAccountName().toLowerCase(Locale.getDefault());
                String reference = expense.getReferenceNumber() == null
                        ? "" : expense.getReferenceNumber().toLowerCase(Locale.getDefault());
                String number = expense.getExpenseNumber() == null
                        ? "" : expense.getExpenseNumber().toLowerCase(Locale.getDefault());
                if (!account.contains(needle) && !reference.contains(needle) && !number.contains(needle)) {
                    continue;
                }
            }
            result.add(expense);
        }
        return result;
    }

    public String getStatusFilter() { return statusFilter; }
    public String getQuery() { return query; }
    public boolean isLoading() { return loading; }
    public boolean isRefreshing() { return refreshing; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isEmpty() {
        return !loading && errorMessage == null && getFilteredExpenses().isEmpty();
    }
}