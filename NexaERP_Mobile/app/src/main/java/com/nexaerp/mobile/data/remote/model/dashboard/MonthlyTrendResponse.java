package com.nexaerp.mobile.data.remote.model.dashboard;

import java.math.BigDecimal;

public class MonthlyTrendResponse {
    private String month;
    private BigDecimal amount;
    public MonthlyTrendResponse() {}
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
