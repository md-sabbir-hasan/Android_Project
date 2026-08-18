package com.nexaerp.mobile.feature.expense;

import android.content.Context;

import com.nexaerp.mobile.R;
import com.nexaerp.mobile.core.formatting.MoneyFormatter;
import com.nexaerp.mobile.data.remote.model.expense.ExpenseResponse;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public final class ExpensePresenter {
    private static final int[] ACCENT_COLORS = {
            R.color.accent_1, R.color.accent_2, R.color.accent_3,
            R.color.accent_4, R.color.accent_5, R.color.accent_6
    };

    private ExpensePresenter() {}

    public static String formattedAmount(ExpenseResponse expense) {
        if (expense == null || expense.getAmount() == null) return "-";
        return MoneyFormatter.format(expense.getAmount(), null);
    }

    public static String formattedDate(ExpenseResponse expense, String unavailableText) {
        if (expense == null || expense.getExpenseDate() == null) return unavailableText;
        return expense.getExpenseDate().format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
        );
    }

    public static String accountLabel(ExpenseResponse expense, String fallback) {
        if (expense == null || expense.getExpenseAccountName() == null
                || expense.getExpenseAccountName().trim().isEmpty()) {
            return fallback;
        }
        return expense.getExpenseAccountName().trim();
    }

    public static int accentColorRes(String seed) {
        if (seed == null || seed.isEmpty()) return ACCENT_COLORS[0];
        return ACCENT_COLORS[Math.abs(seed.hashCode()) % ACCENT_COLORS.length];
    }

    public static String statusLabel(Context context, String status) {
        if (status == null) return "";
        switch (status) {
            case "DRAFT": return context.getString(R.string.expense_status_draft);
            case "POSTED": return context.getString(R.string.expense_status_posted);
            case "CANCELLED": return context.getString(R.string.expense_status_cancelled);
            default: return status;
        }
    }

    public static int statusBackgroundColorRes(String status) {
        if (status == null) return R.color.expense_draft_bg;
        switch (status) {
            case "POSTED": return R.color.expense_posted_bg;
            case "CANCELLED": return R.color.expense_cancelled_bg;
            case "DRAFT":
            default: return R.color.expense_draft_bg;
        }
    }

    public static int statusForegroundColorRes(String status) {
        if (status == null) return R.color.expense_draft_fg;
        switch (status) {
            case "POSTED": return R.color.expense_posted_fg;
            case "CANCELLED": return R.color.expense_cancelled_fg;
            case "DRAFT":
            default: return R.color.expense_draft_fg;
        }
    }

    public static String paymentStatusLabel(Context context, String paymentStatus) {
        if (paymentStatus == null) return "";
        switch (paymentStatus) {
            case "PAID": return context.getString(R.string.expense_payment_paid);
            case "PARTIAL": return context.getString(R.string.expense_payment_partial);
            case "UNPAID": return context.getString(R.string.expense_payment_unpaid);
            default: return paymentStatus;
        }
    }

    public static int paymentStatusBackgroundColorRes(String paymentStatus) {
        if (paymentStatus == null) return R.color.payment_unpaid_bg;
        switch (paymentStatus) {
            case "PAID": return R.color.payment_paid_bg;
            case "PARTIAL": return R.color.payment_partial_bg;
            case "UNPAID":
            default: return R.color.payment_unpaid_bg;
        }
    }

    public static int paymentStatusForegroundColorRes(String paymentStatus) {
        if (paymentStatus == null) return R.color.payment_unpaid_fg;
        switch (paymentStatus) {
            case "PAID": return R.color.payment_paid_fg;
            case "PARTIAL": return R.color.payment_partial_fg;
            case "UNPAID":
            default: return R.color.payment_unpaid_fg;
        }
    }
}