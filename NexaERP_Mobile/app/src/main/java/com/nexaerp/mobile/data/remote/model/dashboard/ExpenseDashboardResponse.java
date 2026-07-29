package com.nexaerp.mobile.data.remote.model.dashboard;

import java.math.BigDecimal;

public class ExpenseDashboardResponse {
    private long draftCount;
    private BigDecimal draftTotalAmount;
    private BigDecimal postedThisMonthTotal;
    private long recurringActiveCount;
    private long recurringDueSoonCount;
    private BigDecimal outstandingDue;
    public ExpenseDashboardResponse() {}
    public long getDraftCount() { return draftCount; }
    public void setDraftCount(long draftCount) { this.draftCount = draftCount; }
    public BigDecimal getDraftTotalAmount() { return draftTotalAmount; }
    public void setDraftTotalAmount(BigDecimal draftTotalAmount) { this.draftTotalAmount = draftTotalAmount; }
    public BigDecimal getPostedThisMonthTotal() { return postedThisMonthTotal; }
    public void setPostedThisMonthTotal(BigDecimal postedThisMonthTotal) { this.postedThisMonthTotal = postedThisMonthTotal; }
    public long getRecurringActiveCount() { return recurringActiveCount; }
    public void setRecurringActiveCount(long recurringActiveCount) { this.recurringActiveCount = recurringActiveCount; }
    public long getRecurringDueSoonCount() { return recurringDueSoonCount; }
    public void setRecurringDueSoonCount(long recurringDueSoonCount) { this.recurringDueSoonCount = recurringDueSoonCount; }
    public BigDecimal getOutstandingDue() { return outstandingDue; }
    public void setOutstandingDue(BigDecimal outstandingDue) { this.outstandingDue = outstandingDue; }
}
