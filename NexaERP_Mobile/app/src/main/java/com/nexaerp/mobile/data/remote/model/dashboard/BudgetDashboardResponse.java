package com.nexaerp.mobile.data.remote.model.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BudgetDashboardResponse {
    private boolean hasActiveBudget;
    private Long activeBudgetId;
    private String activeBudgetName;
    private String unavailableReason;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String currencyCode;
    private BigDecimal totalExpenseBudget;
    private BigDecimal totalExpenseActualYtd;
    private BigDecimal expenseUtilizationPercent;
    private BigDecimal totalRevenueBudget;
    private BigDecimal totalRevenueActualYtd;
    private BigDecimal revenueAchievementPercent;
    private List<BudgetTopAccountResponse> topAccounts;
    public BudgetDashboardResponse() {}
    public boolean isHasActiveBudget() { return hasActiveBudget; }
    public void setHasActiveBudget(boolean hasActiveBudget) { this.hasActiveBudget = hasActiveBudget; }
    public Long getActiveBudgetId() { return activeBudgetId; }
    public void setActiveBudgetId(Long activeBudgetId) { this.activeBudgetId = activeBudgetId; }
    public String getActiveBudgetName() { return activeBudgetName; }
    public void setActiveBudgetName(String activeBudgetName) { this.activeBudgetName = activeBudgetName; }
    public String getUnavailableReason() { return unavailableReason; }
    public void setUnavailableReason(String unavailableReason) { this.unavailableReason = unavailableReason; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public BigDecimal getTotalExpenseBudget() { return totalExpenseBudget; }
    public void setTotalExpenseBudget(BigDecimal totalExpenseBudget) { this.totalExpenseBudget = totalExpenseBudget; }
    public BigDecimal getTotalExpenseActualYtd() { return totalExpenseActualYtd; }
    public void setTotalExpenseActualYtd(BigDecimal totalExpenseActualYtd) { this.totalExpenseActualYtd = totalExpenseActualYtd; }
    public BigDecimal getExpenseUtilizationPercent() { return expenseUtilizationPercent; }
    public void setExpenseUtilizationPercent(BigDecimal expenseUtilizationPercent) { this.expenseUtilizationPercent = expenseUtilizationPercent; }
    public BigDecimal getTotalRevenueBudget() { return totalRevenueBudget; }
    public void setTotalRevenueBudget(BigDecimal totalRevenueBudget) { this.totalRevenueBudget = totalRevenueBudget; }
    public BigDecimal getTotalRevenueActualYtd() { return totalRevenueActualYtd; }
    public void setTotalRevenueActualYtd(BigDecimal totalRevenueActualYtd) { this.totalRevenueActualYtd = totalRevenueActualYtd; }
    public BigDecimal getRevenueAchievementPercent() { return revenueAchievementPercent; }
    public void setRevenueAchievementPercent(BigDecimal revenueAchievementPercent) { this.revenueAchievementPercent = revenueAchievementPercent; }
    public List<BudgetTopAccountResponse> getTopAccounts() { return topAccounts; }
    public void setTopAccounts(List<BudgetTopAccountResponse> topAccounts) { this.topAccounts = topAccounts; }
}
