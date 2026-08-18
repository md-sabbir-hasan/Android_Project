package com.nexaerp.mobile.data.remote.model.expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ExpenseResponse {
    private Long id;
    private String expenseNumber;
    private LocalDate expenseDate;

    private Long expenseAccountId;
    private String expenseAccountName;

    private Long costCenterId;
    private String costCenterCode;
    private String costCenterName;

    private Boolean paidImmediately;

    private Long paymentAccountId;
    private String paymentAccountName;

    private Long partyId;
    private String partyName;

    private BigDecimal amount;
    private BigDecimal paidAmount;
    private BigDecimal dueAmount;
    private String paymentStatus; // UNPAID, PARTIAL, PAID

    private String referenceNumber;
    private String attachmentUrl;
    private String notes;

    private String status; // DRAFT, POSTED, CANCELLED
    private LocalDateTime cancelledAt;
    private String cancelReason;

    private LocalDateTime createdAt;

    private List<BudgetWarningResponse> budgetWarnings;
    private Long recurringTemplateId;

    public ExpenseResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getExpenseNumber() { return expenseNumber; }
    public void setExpenseNumber(String expenseNumber) { this.expenseNumber = expenseNumber; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public Long getExpenseAccountId() { return expenseAccountId; }
    public void setExpenseAccountId(Long expenseAccountId) { this.expenseAccountId = expenseAccountId; }
    public String getExpenseAccountName() { return expenseAccountName; }
    public void setExpenseAccountName(String expenseAccountName) { this.expenseAccountName = expenseAccountName; }
    public Long getCostCenterId() { return costCenterId; }
    public void setCostCenterId(Long costCenterId) { this.costCenterId = costCenterId; }
    public String getCostCenterCode() { return costCenterCode; }
    public void setCostCenterCode(String costCenterCode) { this.costCenterCode = costCenterCode; }
    public String getCostCenterName() { return costCenterName; }
    public void setCostCenterName(String costCenterName) { this.costCenterName = costCenterName; }
    public Boolean getPaidImmediately() { return paidImmediately; }
    public void setPaidImmediately(Boolean paidImmediately) { this.paidImmediately = paidImmediately; }
    public Long getPaymentAccountId() { return paymentAccountId; }
    public void setPaymentAccountId(Long paymentAccountId) { this.paymentAccountId = paymentAccountId; }
    public String getPaymentAccountName() { return paymentAccountName; }
    public void setPaymentAccountName(String paymentAccountName) { this.paymentAccountName = paymentAccountName; }
    public Long getPartyId() { return partyId; }
    public void setPartyId(Long partyId) { this.partyId = partyId; }
    public String getPartyName() { return partyName; }
    public void setPartyName(String partyName) { this.partyName = partyName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public BigDecimal getDueAmount() { return dueAmount; }
    public void setDueAmount(BigDecimal dueAmount) { this.dueAmount = dueAmount; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<BudgetWarningResponse> getBudgetWarnings() { return budgetWarnings; }
    public void setBudgetWarnings(List<BudgetWarningResponse> budgetWarnings) { this.budgetWarnings = budgetWarnings; }
    public Long getRecurringTemplateId() { return recurringTemplateId; }
    public void setRecurringTemplateId(Long recurringTemplateId) { this.recurringTemplateId = recurringTemplateId; }
}