package com.nexaerp.mobile.feature.expense;

import com.nexaerp.mobile.data.remote.model.expense.ExpenseResponse;

public final class ExpenseDetailUiState {
    private final boolean loading;
    private final ExpenseResponse expense;
    private final String errorMessage;
    private final boolean actionInProgress;
    private final String transientMessage;

    private ExpenseDetailUiState(
            boolean loading,
            ExpenseResponse expense,
            String errorMessage,
            boolean actionInProgress,
            String transientMessage
    ) {
        this.loading = loading;
        this.expense = expense;
        this.errorMessage = errorMessage;
        this.actionInProgress = actionInProgress;
        this.transientMessage = transientMessage;
    }

    public static ExpenseDetailUiState loading() {
        return new ExpenseDetailUiState(true, null, null, false, null);
    }

    public ExpenseDetailUiState withExpense(ExpenseResponse expense) {
        return new ExpenseDetailUiState(false, expense, null, false, null);
    }

    public ExpenseDetailUiState withError(String message) {
        return new ExpenseDetailUiState(false, null, message, false, null);
    }

    public ExpenseDetailUiState withActionInProgress(boolean inProgress) {
        return new ExpenseDetailUiState(loading, expense, errorMessage, inProgress, null);
    }

    public ExpenseDetailUiState withTransientError(String message) {
        return new ExpenseDetailUiState(loading, expense, errorMessage, false, message);
    }

    public boolean isLoading() { return loading; }
    public ExpenseResponse getExpense() { return expense; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isActionInProgress() { return actionInProgress; }
    public String getTransientMessage() { return transientMessage; }
}