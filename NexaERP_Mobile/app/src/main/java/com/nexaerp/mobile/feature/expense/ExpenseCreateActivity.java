package com.nexaerp.mobile.feature.expense;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.nexaerp.mobile.R;
import com.nexaerp.mobile.data.remote.api.AccountApi;
import com.nexaerp.mobile.data.remote.api.CostCenterApi;
import com.nexaerp.mobile.data.remote.api.ExpenseApi;
import com.nexaerp.mobile.data.remote.api.PartyApi;
import com.nexaerp.mobile.data.remote.client.RetrofitClient;
import com.nexaerp.mobile.data.repository.ExpenseRepository;
import com.nexaerp.mobile.databinding.ActivityExpenseCreateBinding;
import com.nexaerp.mobile.databinding.RowSelectorBinding;
import com.nexaerp.mobile.feature.common.PickerActivity;
import com.nexaerp.mobile.feature.common.PickerItem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseCreateActivity extends AppCompatActivity {

    private ActivityExpenseCreateBinding binding;
    private ExpenseCreateViewModel viewModel;
    private boolean formPrefillDone;

    private final ActivityResultLauncher<Intent> expenseAccountLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> onPicked(result, viewModel::setExpenseAccount));

    private final ActivityResultLauncher<Intent> costCenterLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> onPicked(result, viewModel::setCostCenter));

    private final ActivityResultLauncher<Intent> paymentAccountLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> onPicked(result, viewModel::setPaymentAccount));

    private final ActivityResultLauncher<Intent> partyLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> onPicked(result, viewModel::setParty));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExpenseCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ExpenseApi expenseApi = RetrofitClient.createService(getApplicationContext(), ExpenseApi.class);
        AccountApi accountApi = RetrofitClient.createService(getApplicationContext(), AccountApi.class);
        CostCenterApi costCenterApi = RetrofitClient.createService(getApplicationContext(), CostCenterApi.class);
        PartyApi partyApi = RetrofitClient.createService(getApplicationContext(), PartyApi.class);
        ExpenseRepository repository = new ExpenseRepository(expenseApi, accountApi, costCenterApi, partyApi);

        viewModel = new ViewModelProvider(
                this, new ExpenseCreateViewModelFactory(repository)
        ).get(ExpenseCreateViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.retryButton.setOnClickListener(ignored -> viewModel.load());
        binding.saveButton.setOnClickListener(ignored -> viewModel.save());

        binding.rowDate.getRoot().setOnClickListener(v -> openDatePicker());
        binding.rowExpenseAccount.getRoot().setOnClickListener(v -> openPicker(
                expenseAccountLauncher, R.string.expense_row_account, currentState().getExpenseAccounts()
        ));
        binding.rowCostCenter.getRoot().setOnClickListener(v -> openPicker(
                costCenterLauncher, R.string.expense_row_cost_center, currentState().getCostCenters()
        ));
        binding.rowPaymentAccount.getRoot().setOnClickListener(v -> openPicker(
                paymentAccountLauncher, R.string.expense_row_payment_method, currentState().getPaymentAccounts()
        ));
        binding.rowParty.getRoot().setOnClickListener(v -> openPicker(
                partyLauncher, R.string.expense_row_party, currentState().getParties()
        ));

        binding.amountInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) syncTextFieldsToViewModel();
        });
        binding.referenceInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) syncTextFieldsToViewModel();
        });
        binding.notesInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) syncTextFieldsToViewModel();
        });

        binding.payToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            viewModel.setPaidImmediately(checkedId == binding.payNowButton.getId());
        });

        viewModel.getState().observe(this, this::render);
        viewModel.load();
    }

    private ExpenseCreateUiState currentState() {
        return viewModel.getState().getValue();
    }

    private void syncTextFieldsToViewModel() {
        viewModel.setAmount(textOf(binding.amountInput));
        viewModel.setReferenceNumber(textOf(binding.referenceInput));
        viewModel.setNotes(textOf(binding.notesInput));
    }

    private String textOf(com.google.android.material.textfield.TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString();
    }

    private void onPicked(ActivityResult result, java.util.function.Consumer<PickerItem> setter) {
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
        PickerItem item = (PickerItem) result.getData().getSerializableExtra(PickerActivity.EXTRA_RESULT_ITEM);
        if (item != null) setter.accept(item);
    }

    private void openPicker(
            ActivityResultLauncher<Intent> launcher, int titleRes, List<PickerItem> items
    ) {
        launcher.launch(PickerActivity.newIntent(this, getString(titleRes), new ArrayList<>(items)));
    }

    private void openDatePicker() {
        LocalDate current = currentState() == null ? LocalDate.now() : currentState().getDate();
        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) ->
                        viewModel.setDate(LocalDate.of(year, month + 1, dayOfMonth)),
                current.getYear(), current.getMonthValue() - 1, current.getDayOfMonth()
        ).show();
    }

    private void render(ExpenseCreateUiState state) {
        boolean fatal = state.getLoadError() != null;
        binding.loadingState.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
        binding.errorState.setVisibility(fatal ? View.VISIBLE : View.GONE);
        binding.formContent.setVisibility(!state.isLoading() && !fatal ? View.VISIBLE : View.GONE);

        if (fatal) {
            binding.errorMessage.setText(state.getLoadError());
            return;
        }
        if (state.isLoading()) return;

        if (!formPrefillDone) {
            binding.amountInput.setText(state.getAmount());
            binding.referenceInput.setText(state.getReferenceNumber());
            binding.notesInput.setText(state.getNotes());
            formPrefillDone = true;
        }

        setRow(binding.rowDate, R.string.expense_row_date, state.getDate().format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
        ));
        setRow(binding.rowExpenseAccount, R.string.expense_row_account,
                state.getExpenseAccount() == null
                        ? getString(R.string.expense_tap_to_select)
                        : state.getExpenseAccount().getTitle());
        setRow(binding.rowCostCenter, R.string.expense_row_cost_center,
                state.getCostCenter() == null
                        ? getString(R.string.expense_optional_tap_to_select)
                        : state.getCostCenter().getTitle());

        boolean payNow = state.isPaidImmediately();
        if (binding.payToggleGroup.getCheckedButtonId() == View.NO_ID) {
            binding.payToggleGroup.check(payNow ? binding.payNowButton.getId() : binding.payLaterButton.getId());
        }
        binding.rowPaymentAccount.getRoot().setVisibility(payNow ? View.VISIBLE : View.GONE);
        binding.rowParty.getRoot().setVisibility(payNow ? View.GONE : View.VISIBLE);

        setRow(binding.rowPaymentAccount, R.string.expense_row_payment_method,
                state.getPaymentAccount() == null
                        ? getString(R.string.expense_tap_to_select)
                        : state.getPaymentAccount().getTitle());
        setRow(binding.rowParty, R.string.expense_row_party,
                state.getParty() == null
                        ? getString(R.string.expense_optional_tap_to_select)
                        : state.getParty().getTitle());

        binding.saveButton.setEnabled(!state.isSaving());
        binding.saveButton.setText(state.isSaving() ? R.string.role_saving : R.string.expense_save);

        if (state.getErrorMessage() != null) {
            Snackbar.make(binding.getRoot(), state.getErrorMessage(), Snackbar.LENGTH_LONG).show();
        }

        if (state.isSaved()) {
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    private void setRow(RowSelectorBinding row, int labelRes, String value) {
        row.selectorLabel.setText(labelRes);
        row.selectorValue.setText(value);
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }

    public static Intent newIntent(Activity activity) {
        return new Intent(activity, ExpenseCreateActivity.class);
    }
}