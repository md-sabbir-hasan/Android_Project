package com.nexaerp.mobile.data.remote.model.expense;

import java.math.BigDecimal;

public class BudgetWarningResponse {
    private Long budgetId;
    private Long accountId;
    private String accountCode;
    private String accountName;
    private Long accountingPeriodId;
    private String accountingPeriodName;
    private BigDecimal budgetAmount;
    private BigDecimal actualBeforePosting;
    private BigDecimal transactionAmount;
    private BigDecimal projectedActual;
    private BigDecimal exceededAmount;
    private String message;

    public BudgetWarningResponse() {}

    public Long getBudgetId() { return budgetId; }
    public void setBudgetId(Long budgetId) { this.budgetId = budgetId; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getAccountCode() { return accountCode; }
    public void setAccountCode(String accountCode) { this.accountCode = accountCode; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public Long getAccountingPeriodId() { return accountingPeriodId; }
    public void setAccountingPeriodId(Long accountingPeriodId) { this.accountingPeriodId = accountingPeriodId; }
    public String getAccountingPeriodName() { return accountingPeriodName; }
    public void setAccountingPeriodName(String accountingPeriodName) { this.accountingPeriodName = accountingPeriodName; }
    public BigDecimal getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }
    public BigDecimal getActualBeforePosting() { return actualBeforePosting; }
    public void setActualBeforePosting(BigDecimal actualBeforePosting) { this.actualBeforePosting = actualBeforePosting; }
    public BigDecimal getTransactionAmount() { return transactionAmount; }
    public void setTransactionAmount(BigDecimal transactionAmount) { this.transactionAmount = transactionAmount; }
    public BigDecimal getProjectedActual() { return projectedActual; }
    public void setProjectedActual(BigDecimal projectedActual) { this.projectedActual = projectedActual; }
    public BigDecimal getExceededAmount() { return exceededAmount; }
    public void setExceededAmount(BigDecimal exceededAmount) { this.exceededAmount = exceededAmount; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}