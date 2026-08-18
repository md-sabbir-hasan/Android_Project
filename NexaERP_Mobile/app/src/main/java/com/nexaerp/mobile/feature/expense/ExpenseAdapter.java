package com.nexaerp.mobile.feature.expense;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nexaerp.mobile.R;
import com.nexaerp.mobile.data.remote.model.expense.ExpenseResponse;
import com.nexaerp.mobile.databinding.ItemExpenseBinding;

import java.util.ArrayList;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    public interface OnExpenseClickListener {
        void onExpenseClicked(ExpenseResponse expense);
    }

    private final List<ExpenseResponse> items = new ArrayList<>();
    private final OnExpenseClickListener listener;

    public ExpenseAdapter(OnExpenseClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ExpenseResponse> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExpenseBinding binding = ItemExpenseBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemExpenseBinding binding;

        ViewHolder(ItemExpenseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ExpenseResponse expense, OnExpenseClickListener listener) {
            Context context = binding.getRoot().getContext();
            String accountLabel = ExpensePresenter.accountLabel(
                    expense, context.getString(R.string.expense_unnamed_account)
            );

            binding.expenseAccount.setText(accountLabel);
            binding.accentBar.setBackgroundColor(
                    ContextCompat.getColor(context, ExpensePresenter.accentColorRes(accountLabel))
            );

            String number = expense.getExpenseNumber() == null ? "" : expense.getExpenseNumber();
            String date = ExpensePresenter.formattedDate(
                    expense, context.getString(R.string.notification_time_unavailable)
            );
            binding.expenseMeta.setText(number.isEmpty() ? date : number + " · " + date);

            binding.expenseAmount.setText(ExpensePresenter.formattedAmount(expense));

            binding.expenseStatusPill.setText(ExpensePresenter.statusLabel(context, expense.getStatus()));
            tint(context, binding.expenseStatusPill.getBackground(),
                    ExpensePresenter.statusBackgroundColorRes(expense.getStatus()));
            binding.expenseStatusPill.setTextColor(
                    ContextCompat.getColor(context, ExpensePresenter.statusForegroundColorRes(expense.getStatus()))
            );

            boolean showPaymentPill = "POSTED".equals(expense.getStatus())
                    && expense.getPaymentStatus() != null;
            binding.expensePaymentPill.setVisibility(showPaymentPill ? View.VISIBLE : View.GONE);
            if (showPaymentPill) {
                binding.expensePaymentPill.setText(
                        ExpensePresenter.paymentStatusLabel(context, expense.getPaymentStatus())
                );
                tint(context, binding.expensePaymentPill.getBackground(),
                        ExpensePresenter.paymentStatusBackgroundColorRes(expense.getPaymentStatus()));
                binding.expensePaymentPill.setTextColor(ContextCompat.getColor(
                        context, ExpensePresenter.paymentStatusForegroundColorRes(expense.getPaymentStatus())
                ));
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onExpenseClicked(expense);
            });
        }

        private void tint(Context context, Drawable background, int colorRes) {
            if (background instanceof GradientDrawable) {
                ((GradientDrawable) background.mutate()).setColor(ContextCompat.getColor(context, colorRes));
            }
        }
    }
}