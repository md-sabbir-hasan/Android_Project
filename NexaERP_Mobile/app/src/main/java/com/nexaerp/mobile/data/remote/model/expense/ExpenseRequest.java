package com.nexaerp.mobile.data.remote.model.expense;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseRequest {
    private LocalDate expenseDate;
    private Long expenseAccountId;
    private Long costCenterId;
    private Boolean paidImmediately;
    private Long paymentAccountId;
    private Long partyId;
    private BigDecimal amount;
    private String referenceNumber;
    private String attachmentUrl;
    private String notes;

    public ExpenseRequest() {}

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public Long getExpenseAccountId() { return expenseAccountId; }
    public void setExpenseAccountId(Long expenseAccountId) { this.expenseAccountId = expenseAccountId; }
    public Long getCostCenterId() { return costCenterId; }
    public void setCostCenterId(Long costCenterId) { this.costCenterId = costCenterId; }
    public Boolean getPaidImmediately() { return paidImmediately; }
    public void setPaidImmediately(Boolean paidImmediately) { this.paidImmediately = paidImmediately; }
    public Long getPaymentAccountId() { return paymentAccountId; }
    public void setPaymentAccountId(Long paymentAccountId) { this.paymentAccountId = paymentAccountId; }
    public Long getPartyId() { return partyId; }
    public void setPartyId(Long partyId) { this.partyId = partyId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}