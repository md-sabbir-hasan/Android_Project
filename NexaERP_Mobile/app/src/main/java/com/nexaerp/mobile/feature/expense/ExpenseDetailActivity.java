package com.nexaerp.mobile.feature.expense;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.nexaerp.mobile.R;
import com.nexaerp.mobile.data.remote.api.AccountApi;
import com.nexaerp.mobile.data.remote.api.CostCenterApi;
import com.nexaerp.mobile.data.remote.api.ExpenseApi;
import com.nexaerp.mobile.data.remote.api.PartyApi;
import com.nexaerp.mobile.data.remote.client.RetrofitClient;
import com.nexaerp.mobile.data.remote.model.expense.BudgetWarningResponse;
import com.nexaerp.mobile.data.remote.model.expense.ExpenseResponse;
import com.nexaerp.mobile.data.repository.ExpenseRepository;
import com.nexaerp.mobile.databinding.ActivityExpenseDetailBinding;

import java.util.List;

public class ExpenseDetailActivity extends AppCompatActivity {

    private static final String EXTRA_EXPENSE_ID = "extra_expense_id";

    private ActivityExpenseDetailBinding binding;
    private ExpenseDetailViewModel viewModel;
    private long expenseId;
    private String lastShownTransientMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExpenseDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        expenseId = getIntent().getLongExtra(EXTRA_EXPENSE_ID, 0);

        ExpenseApi expenseApi = RetrofitClient.createService(getApplicationContext(), ExpenseApi.class);
        AccountApi accountApi = RetrofitClient.createService(getApplicationContext(), AccountApi.class);
        CostCenterApi costCenterApi = RetrofitClient.createService(getApplicationContext(), CostCenterApi.class);
        PartyApi partyApi = RetrofitClient.createService(getApplicationContext(), PartyApi.class);
        ExpenseRepository repository = new ExpenseRepository(expenseApi, accountApi, costCenterApi, partyApi);

        viewModel = new ViewModelProvider(
                this, new ExpenseDetailViewModelFactory(repository, expenseId)
        ).get(ExpenseDetailViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finishWithResult());
        binding.retryButton.setOnClickListener(ignored -> viewModel.load());
        binding.postButton.setOnClickListener(ignored -> confirmPost());
        binding.cancelButton.setOnClickListener(ignored -> promptCancelReason());

        viewModel.getState().observe(this, this::render);
        viewModel.load();
    }

    private void render(ExpenseDetailUiState state) {
        binding.loadingState.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
        boolean fatal = state.getErrorMessage() != null;
        binding.errorState.setVisibility(fatal ? View.VISIBLE : View.GONE);
        binding.content.setVisibility(!state.isLoading() && !fatal ? View.VISIBLE : View.GONE);

        if (fatal) {
            binding.errorMessage.setText(state.getErrorMessage());
            return;
        }
        if (state.getExpense() == null) return;

        ExpenseResponse expense = state.getExpense();

        binding.expenseNumber.setText(
                expense.getExpenseNumber() == null ? "" : expense.getExpenseNumber()
        );
        binding.expenseAmount.setText(ExpensePresenter.formattedAmount(expense));

        binding.expenseStatusPill.setText(ExpensePresenter.statusLabel(this, expense.getStatus()));
        tint(binding.expenseStatusPill.getBackground(),
                ExpensePresenter.statusBackgroundColorRes(expense.getStatus()));
        binding.expenseStatusPill.setTextColor(
                ContextCompat.getColor(this, ExpensePresenter.statusForegroundColorRes(expense.getStatus()))
        );

        boolean showPaymentPill = "POSTED".equals(expense.getStatus()) && expense.getPaymentStatus() != null;
        binding.expensePaymentPill.setVisibility(showPaymentPill ? View.VISIBLE : View.GONE);
        if (showPaymentPill) {
            binding.expensePaymentPill.setText(
                    ExpensePresenter.paymentStatusLabel(this, expense.getPaymentStatus())
            );
            tint(binding.expensePaymentPill.getBackground(),
                    ExpensePresenter.paymentStatusBackgroundColorRes(expense.getPaymentStatus()));
            binding.expensePaymentPill.setTextColor(ContextCompat.getColor(
                    this, ExpensePresenter.paymentStatusForegroundColorRes(expense.getPaymentStatus())
            ));
        }

        renderBudgetWarnings(expense);
        renderRows(expense);

        boolean isDraft = "DRAFT".equals(expense.getStatus());
        binding.postButton.setVisibility(isDraft ? View.VISIBLE : View.GONE);
        binding.cancelButton.setVisibility(isDraft ? View.VISIBLE : View.GONE);
        binding.postButton.setEnabled(!state.isActionInProgress());
        binding.cancelButton.setEnabled(!state.isActionInProgress());

        boolean cancelled = "CANCELLED".equals(expense.getStatus())
                && expense.getCancelReason() != null && !expense.getCancelReason().trim().isEmpty();
        binding.cancelReasonText.setVisibility(cancelled ? View.VISIBLE : View.GONE);
        if (cancelled) {
            binding.cancelReasonText.setText(
                    getString(R.string.expense_cancelled_reason_prefix) + " " + expense.getCancelReason()
            );
        }

        if (state.getTransientMessage() != null
                && !state.getTransientMessage().equals(lastShownTransientMessage)) {
            lastShownTransientMessage = state.getTransientMessage();
            Snackbar.make(binding.getRoot(), state.getTransientMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private void renderBudgetWarnings(ExpenseResponse expense) {
        List<BudgetWarningResponse> warnings = expense.getBudgetWarnings();
        boolean hasWarnings = warnings != null && !warnings.isEmpty();
        binding.budgetWarningBanner.setVisibility(hasWarnings ? View.VISIBLE : View.GONE);
        if (!hasWarnings) return;

        StringBuilder combined = new StringBuilder();
        for (BudgetWarningResponse warning : warnings) {
            if (combined.length() > 0) combined.append("\n");
            combined.append(warning.getMessage() == null ? "" : warning.getMessage());
        }
        binding.budgetWarningText.setText(combined.toString());
    }

    private void renderRows(ExpenseResponse expense) {
        setRow(binding.rowDate, R.string.expense_row_date,
                ExpensePresenter.formattedDate(expense, getString(R.string.notification_time_unavailable)));
        setRow(binding.rowAccount, R.string.expense_row_account,
                ExpensePresenter.accountLabel(expense, getString(R.string.expense_unnamed_account)));
        setRow(binding.rowCostCenter, R.string.expense_row_cost_center,
                expense.getCostCenterName() == null ? getString(R.string.expense_none) : expense.getCostCenterName());

        boolean paidNow = Boolean.TRUE.equals(expense.getPaidImmediately());
        setRow(binding.rowPaymentMethod, R.string.expense_row_payment_method,
                paidNow
                        ? getString(R.string.expense_paid_now_with,
                        expense.getPaymentAccountName() == null ? "" : expense.getPaymentAccountName())
                        : getString(R.string.expense_pay_later));
        setRow(binding.rowParty, R.string.expense_row_party,
                expense.getPartyName() == null ? getString(R.string.expense_none) : expense.getPartyName());
        setRow(binding.rowReference, R.string.expense_row_reference,
                expense.getReferenceNumber() == null || expense.getReferenceNumber().trim().isEmpty()
                        ? getString(R.string.expense_none) : expense.getReferenceNumber());
        setRow(binding.rowNotes, R.string.expense_row_notes,
                expense.getNotes() == null || expense.getNotes().trim().isEmpty()
                        ? getString(R.string.expense_none) : expense.getNotes());
    }

    private void setRow(com.nexaerp.mobile.databinding.RowLabelValueBinding row, int labelRes, String value) {
        row.rowLabel.setText(labelRes);
        row.rowValue.setText(value);
    }

    private void tint(android.graphics.drawable.Drawable background, int colorRes) {
        if (background instanceof GradientDrawable) {
            ((GradientDrawable) background.mutate()).setColor(ContextCompat.getColor(this, colorRes));
        }
    }

    private void confirmPost() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.expense_post)
                .setMessage(R.string.expense_post_confirm_message)
                .setPositiveButton(R.string.expense_post, (dialog, which) -> viewModel.post())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void promptCancelReason() {
        EditText input = new EditText(this);
        input.setHint(R.string.expense_cancel_reason_hint);
        int padding = Math.round(20 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle(R.string.expense_cancel)
                .setView(input)
                .setPositiveButton(R.string.expense_cancel, (dialog, which) -> {
                    String reason = input.getText() == null ? "" : input.getText().toString().trim();
                    if (reason.isEmpty()) {
                        Snackbar.make(binding.getRoot(), R.string.expense_cancel_reason_required,
                                Snackbar.LENGTH_LONG).show();
                        return;
                    }
                    viewModel.cancel(reason);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void finishWithResult() {
        setResult(viewModel.hasChanges() ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    public void onBackPressed() {
        finishWithResult();
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }

    public static Intent newIntent(Activity activity, long expenseId) {
        Intent intent = new Intent(activity, ExpenseDetailActivity.class);
        intent.putExtra(EXTRA_EXPENSE_ID, expenseId);
        return intent;
    }
}