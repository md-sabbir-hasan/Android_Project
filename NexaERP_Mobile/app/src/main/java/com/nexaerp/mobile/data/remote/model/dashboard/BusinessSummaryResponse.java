package com.nexaerp.mobile.data.remote.model.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BusinessSummaryResponse {
    private BigDecimal cashPosition;
    private Boolean cashConfigured;
    private LocalDate asOfDate;
    private String currencyCode;
    private BigDecimal accountsReceivable;
    private Long overdueInvoiceCount;
    private BigDecimal overdueInvoiceAmount;
    private BigDecimal accountsPayable;
    private Long overdueBillCount;
    private BigDecimal overdueBillAmount;
    private List<MonthlyTrendResponse> revenueTrend;
    private List<MonthlyTrendResponse> expenseTrend;
    private LocalDate trendFromDate;
    private LocalDate trendToDate;
    public BusinessSummaryResponse() {}
    public BigDecimal getCashPosition() { return cashPosition; }
    public void setCashPosition(BigDecimal cashPosition) { this.cashPosition = cashPosition; }
    public Boolean getCashConfigured() { return cashConfigured; }
    public void setCashConfigured(Boolean cashConfigured) { this.cashConfigured = cashConfigured; }
    public LocalDate getAsOfDate() { return asOfDate; }
    public void setAsOfDate(LocalDate asOfDate) { this.asOfDate = asOfDate; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public BigDecimal getAccountsReceivable() { return accountsReceivable; }
    public void setAccountsReceivable(BigDecimal accountsReceivable) { this.accountsReceivable = accountsReceivable; }
    public Long getOverdueInvoiceCount() { return overdueInvoiceCount; }
    public void setOverdueInvoiceCount(Long overdueInvoiceCount) { this.overdueInvoiceCount = overdueInvoiceCount; }
    public BigDecimal getOverdueInvoiceAmount() { return overdueInvoiceAmount; }
    public void setOverdueInvoiceAmount(BigDecimal overdueInvoiceAmount) { this.overdueInvoiceAmount = overdueInvoiceAmount; }
    public BigDecimal getAccountsPayable() { return accountsPayable; }
    public void setAccountsPayable(BigDecimal accountsPayable) { this.accountsPayable = accountsPayable; }
    public Long getOverdueBillCount() { return overdueBillCount; }
    public void setOverdueBillCount(Long overdueBillCount) { this.overdueBillCount = overdueBillCount; }
    public BigDecimal getOverdueBillAmount() { return overdueBillAmount; }
    public void setOverdueBillAmount(BigDecimal overdueBillAmount) { this.overdueBillAmount = overdueBillAmount; }
    public List<MonthlyTrendResponse> getRevenueTrend() { return revenueTrend; }
    public void setRevenueTrend(List<MonthlyTrendResponse> revenueTrend) { this.revenueTrend = revenueTrend; }
    public List<MonthlyTrendResponse> getExpenseTrend() { return expenseTrend; }
    public void setExpenseTrend(List<MonthlyTrendResponse> expenseTrend) { this.expenseTrend = expenseTrend; }
    public LocalDate getTrendFromDate() { return trendFromDate; }
    public void setTrendFromDate(LocalDate trendFromDate) { this.trendFromDate = trendFromDate; }
    public LocalDate getTrendToDate() { return trendToDate; }
    public void setTrendToDate(LocalDate trendToDate) { this.trendToDate = trendToDate; }
}
