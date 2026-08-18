package com.nexaerp.mobile.data.remote.model.expense;

public class ExpenseCancelRequest {
    private String reason;

    public ExpenseCancelRequest() {}

    public ExpenseCancelRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}