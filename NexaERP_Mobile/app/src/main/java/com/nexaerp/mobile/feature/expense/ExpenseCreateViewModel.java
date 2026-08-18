package com.nexaerp.mobile.feature.expense;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nexaerp.mobile.data.remote.model.account.AccountResponse;
import com.nexaerp.mobile.data.remote.model.costcenter.CostCenterResponse;
import com.nexaerp.mobile.data.remote.model.expense.ExpenseRequest;
import com.nexaerp.mobile.data.remote.model.party.PartyResponse;
import com.nexaerp.mobile.data.repository.ExpenseRepository;
import com.nexaerp.mobile.feature.common.PickerItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseCreateViewModel extends ViewModel {
    private final ExpenseRepository repository;
    private final MutableLiveData<ExpenseCreateUiState> state =
            new MutableLiveData<>(ExpenseCreateUiState.initial());

    public ExpenseCreateViewModel(ExpenseRepository repository) {
        this.repository = repository;
    }

    public LiveData<ExpenseCreateUiState> getState() {
        return state;
    }

    public void load() {
        repository.loadExpenseAccounts(expenseAccountResult -> {
            if (!expenseAccountResult.isSuccess()) {
                failLoad(expenseAccountResult.getErrorMessage());
                return;
            }
            List<PickerItem> expenseAccounts = toAccountItems(expenseAccountResult.getItems());

            repository.loadPaymentAccounts(paymentAccountResult -> {
                if (!paymentAccountResult.isSuccess()) {
                    failLoad(paymentAccountResult.getErrorMessage());
                    return;
                }
                List<PickerItem> paymentAccounts = toAccountItems(paymentAccountResult.getItems());

                repository.loadCostCenters(costCenterResult -> {
                    if (!costCenterResult.isSuccess()) {
                        failLoad(costCenterResult.getErrorMessage());
                        return;
                    }
                    List<PickerItem> costCenters = new ArrayList<>();
                    for (CostCenterResponse costCenter : costCenterResult.getItems()) {
                        if (costCenter.getId() == null) continue;
                        costCenters.add(new PickerItem(
                                costCenter.getId(),
                                costCenter.getName(),
                                costCenter.getCode()
                        ));
                    }

                    repository.loadParties(partyResult -> {
                        if (!partyResult.isSuccess()) {
                            failLoad(partyResult.getErrorMessage());
                            return;
                        }
                        List<PickerItem> parties = new ArrayList<>();
                        for (PartyResponse party : partyResult.getItems()) {
                            if (party.getId() == null) continue;
                            parties.add(new PickerItem(party.getId(), party.getName(), party.getType()));
                        }

                        ExpenseCreateUiState current = state.getValue();
                        if (current != null) {
                            state.setValue(current.withLoaded(
                                    expenseAccounts, paymentAccounts, costCenters, parties
                            ));
                        }
                    });
                });
            });
        });
    }

    private void failLoad(String message) {
        ExpenseCreateUiState current = state.getValue();
        if (current != null) state.setValue(current.withLoadError(message));
    }

    private List<PickerItem> toAccountItems(List<AccountResponse> accounts) {
        List<PickerItem> items = new ArrayList<>();
        for (AccountResponse account : accounts) {
            if (account.getId() == null) continue;
            items.add(new PickerItem(account.getId(), account.getName(), account.getCode()));
        }
        return items;
    }

    public void setDate(LocalDate date) {
        update(current -> current.withDate(date));
    }

    public void setExpenseAccount(PickerItem item) {
        update(current -> current.withExpenseAccount(item));
    }

    public void setCostCenter(PickerItem item) {
        update(current -> current.withCostCenter(item));
    }

    public void setPaidImmediately(boolean value) {
        update(current -> current.withPaidImmediately(value));
    }

    public void setPaymentAccount(PickerItem item) {
        update(current -> current.withPaymentAccount(item));
    }

    public void setParty(PickerItem item) {
        update(current -> current.withParty(item));
    }

    public void setAmount(String amount) {
        update(current -> current.withAmount(amount));
    }

    public void setReferenceNumber(String value) {
        update(current -> current.withReferenceNumber(value));
    }

    public void setNotes(String value) {
        update(current -> current.withNotes(value));
    }

    private interface Updater {
        ExpenseCreateUiState apply(ExpenseCreateUiState current);
    }

    private void update(Updater updater) {
        ExpenseCreateUiState current = state.getValue();
        if (current != null) state.setValue(updater.apply(current));
    }

    public void save() {
        ExpenseCreateUiState current = state.getValue();
        if (current == null || current.isSaving()) return;

        if (current.getExpenseAccount() == null) {
            state.setValue(current.withSaveError("Select an expense account."));
            return;
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(current.getAmount().trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            state.setValue(current.withSaveError("Enter a valid amount greater than zero."));
            return;
        }
        if (current.isPaidImmediately() && current.getPaymentAccount() == null) {
            state.setValue(current.withSaveError("Select a payment account."));
            return;
        }

        state.setValue(current.withSaving());

        ExpenseRequest request = new ExpenseRequest();
        request.setExpenseDate(current.getDate());
        request.setExpenseAccountId(current.getExpenseAccount().getId());
        request.setCostCenterId(current.getCostCenter() == null ? null : current.getCostCenter().getId());
        request.setPaidImmediately(current.isPaidImmediately());
        request.setPaymentAccountId(
                current.isPaidImmediately() && current.getPaymentAccount() != null
                        ? current.getPaymentAccount().getId() : null
        );
        request.setPartyId(current.getParty() == null ? null : current.getParty().getId());
        request.setAmount(amount);
        request.setReferenceNumber(blankToNull(current.getReferenceNumber()));
        request.setNotes(blankToNull(current.getNotes()));

        repository.createExpense(request, result -> {
            ExpenseCreateUiState latest = state.getValue();
            if (latest == null) return;
            if (result.isSuccess()) {
                state.setValue(latest.withSaved());
            } else {
                state.setValue(latest.withSaveError(result.getErrorMessage()));
            }
        });
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}