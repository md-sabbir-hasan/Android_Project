package com.nexaerp.mobile.core.formatting;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public final class MoneyFormatter {
    private MoneyFormatter() {}

    public static String format(BigDecimal amount, String currencyCode) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(2);
        String number = formatter.format(amount);
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            return number;
        }
        try {
            Currency currency = Currency.getInstance(currencyCode.trim());
            return currency.getCurrencyCode() + " " + number;
        } catch (IllegalArgumentException ignored) {
            return currencyCode.trim() + " " + number;
        }
    }
}
