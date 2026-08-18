package com.nexaerp.mobile.feature.expense;

import com.nexaerp.mobile.feature.common.PickerItem;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public final class ExpenseCreateUiState {
    private final boolean loading;
    private final String loadError;

    private final List<PickerItem> expenseAccounts;
    private final List<PickerItem> paymentAccounts;
    private final List<PickerItem> costCenters;
    private final List<PickerItem> parties;

    private final LocalDate date;
    private final PickerItem expenseAccount;
    private final PickerItem costCenter;
    private final boolean paidImmediately;
    private final PickerItem paymentAccount;
    private final PickerItem party;
    private final String amount;
    private final String referenceNumber;
    private final String notes;

    private final boolean saving;
    private final String errorMessage;
    private final boolean saved;

    private ExpenseCreateUiState(
            boolean loading, String loadError,
            List<PickerItem> expenseAccounts, List<PickerItem> paymentAccounts,
            List<PickerItem> costCenters, List<PickerItem> parties,
            LocalDate date, PickerItem expenseAccount, PickerItem costCenter,
            boolean paidImmediately, PickerItem paymentAccount, PickerItem party,
            String amount, String referenceNumber, String notes,
            boolean saving, String errorMessage, boolean saved
    ) {
        this.loading = loading;
        this.loadError = loadError;
        this.expenseAccounts = expenseAccounts;
        this.paymentAccounts = paymentAccounts;
        this.costCenters = costCenters;
        this.parties = parties;
        this.date = date;
        this.expenseAccount = expenseAccount;
        this.costCenter = costCenter;
        this.paidImmediately = paidImmediately;
        this.paymentAccount = paymentAccount;
        this.party = party;
        this.amount = amount;
        this.referenceNumber = referenceNumber;
        this.notes = notes;
        this.saving = saving;
        this.errorMessage = errorMessage;
        this.saved = saved;
    }

    public static ExpenseCreateUiState initial() {
        return new ExpenseCreateUiState(
                true, null,
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                LocalDate.now(), null, null,
                true, null, null,
                "", "", "",
                false, null, false
        );
    }

    public ExpenseCreateUiState withLoaded(
            List<PickerItem> expenseAccounts, List<PickerItem> paymentAccounts,
            List<PickerItem> costCenters, List<PickerItem> parties
    ) {
        return new ExpenseCreateUiState(
                false, null, expenseAccounts, paymentAccounts, costCenters, parties,
                date, expenseAccount, costCenter, paidImmediately, paymentAccount, party,
                amount, referenceNumber, notes, saving, null, saved
        );
    }

    public ExpenseCreateUiState withLoadError(String message) {
        return new ExpenseCreateUiState(
                false, message, expenseAccounts, paymentAccounts, costCenters, parties,
                date, expenseAccount, costCenter, paidImmediately, paymentAccount, party,
                amount, referenceNumber, notes, saving, null, saved
        );
    }

    public ExpenseCreateUiState withDate(LocalDate newDate) {
        return copy(newDate, expenseAccount, costCenter, paidImmediately, paymentAccount, party,
                amount, referenceNumber, notes, null);
    }

    public ExpenseCreateUiState withExpenseAccount(PickerItem item) {
        return copy(date, item, costCenter, paidImmediately, paymentAccount, party,
                amount, referenceNumber, notes, null);
    }

    public ExpenseCreateUiState withCostCenter(PickerItem item) {
        return copy(date, expenseAccount, item, paidImmediately, paymentAccount, party,
                amount, referenceNumber, notes, null);
    }

    public ExpenseCreateUiState withPaidImmediately(boolean value) {
        return copy(date, expenseAccount, costCenter, value, paymentAccount, party,
                amount, referenceNumber, notes, null);
    }

    public ExpenseCreateUiState withPaymentAccount(PickerItem item) {
        return copy(date, expenseAccount, costCenter, paidImmediately, item, party,
                amount, referenceNumber, notes, null);
    }

    public ExpenseCreateUiState withParty(PickerItem item) {
        return copy(date, expenseAccount, costCenter, paidImmediately, paymentAccount, item,
                amount, referenceNumber, notes, null);
    }

    public ExpenseCreateUiState withAmount(String value) {
        return copy(date, expenseAccount, costCenter, paidImmediately, paymentAccount, party,
                value, referenceNumber, notes, null);
    }

    public ExpenseCreateUiState withReferenceNumber(String value) {
        return copy(date, expenseAccount, costCenter, paidImmediately, paymentAccount, party,
                amount, value, notes, null);
    }

    public ExpenseCreateUiState withNotes(String value) {
        return copy(date, expenseAccount, costCenter, paidImmediately, paymentAccount, party,
                amount, referenceNumber, value, null);
    }

    public ExpenseCreateUiState withSaving() {
        return new ExpenseCreateUiState(
                loading, loadError, expenseAccounts, paymentAccounts, costCenters, parties,
                date, expenseAccount, costCenter, paidImmediately, paymentAccount, party,
                amount, referenceNumber, notes, true, null, false
        );
    }

    public ExpenseCreateUiState withSaved() {
        return new ExpenseCreateUiState(
                loading, loadError, expenseAccounts, paymentAccounts, costCenters, parties,
                date, expenseAccount, costCenter, paidImmediately, paymentAccount, party,
                amount, referenceNumber, notes, false, null, true
        );
    }

    public ExpenseCreateUiState withSaveError(String message) {
        return new ExpenseCreateUiState(
                loading, loadError, expenseAccounts, paymentAccounts, costCenters, parties,
                date, expenseAccount, costCenter, paidImmediately, paymentAccount, party,
                amount, referenceNumber, notes, false, message, false
        );
    }

    private ExpenseCreateUiState copy(
            LocalDate date, PickerItem expenseAccount, PickerItem costCenter,
            boolean paidImmediately, PickerItem paymentAccount, PickerItem party,
            String amount, String referenceNumber, String notes, String errorMessage
    ) {
        return new ExpenseCreateUiState(
                loading, loadError, expenseAccounts, paymentAccounts, costCenters, parties,
                date, expenseAccount, costCenter, paidImmediately, paymentAccount, party,
                amount, referenceNumber, notes, saving, errorMessage, false
        );
    }

    public boolean isLoading() { return loading; }
    public String getLoadError() { return loadError; }
    public List<PickerItem> getExpenseAccounts() { return expenseAccounts; }
    public List<PickerItem> getPaymentAccounts() { return paymentAccounts; }
    public List<PickerItem> getCostCenters() { return costCenters; }
    public List<PickerItem> getParties() { return parties; }
    public LocalDate getDate() { return date; }
    public PickerItem getExpenseAccount() { return expenseAccount; }
    public PickerItem getCostCenter() { return costCenter; }
    public boolean isPaidImmediately() { return paidImmediately; }
    public PickerItem getPaymentAccount() { return paymentAccount; }
    public PickerItem getParty() { return party; }
    public String getAmount() { return amount; }
    public String getReferenceNumber() { return referenceNumber; }
    public String getNotes() { return notes; }
    public boolean isSaving() { return saving; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isSaved() { return saved; }
}