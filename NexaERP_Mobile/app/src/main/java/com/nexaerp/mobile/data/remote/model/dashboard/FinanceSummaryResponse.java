package com.nexaerp.mobile.data.remote.model.dashboard;

public class FinanceSummaryResponse {
    private Long totalAccounts;
    private Long totalJournalEntries;
    private Long postedJournalEntries;
    private Long draftJournalEntries;
    private Long reversedJournalEntries;
    public FinanceSummaryResponse() {}
    public Long getTotalAccounts() { return totalAccounts; }
    public void setTotalAccounts(Long totalAccounts) { this.totalAccounts = totalAccounts; }
    public Long getTotalJournalEntries() { return totalJournalEntries; }
    public void setTotalJournalEntries(Long totalJournalEntries) { this.totalJournalEntries = totalJournalEntries; }
    public Long getPostedJournalEntries() { return postedJournalEntries; }
    public void setPostedJournalEntries(Long postedJournalEntries) { this.postedJournalEntries = postedJournalEntries; }
    public Long getDraftJournalEntries() { return draftJournalEntries; }
    public void setDraftJournalEntries(Long draftJournalEntries) { this.draftJournalEntries = draftJournalEntries; }
    public Long getReversedJournalEntries() { return reversedJournalEntries; }
    public void setReversedJournalEntries(Long reversedJournalEntries) { this.reversedJournalEntries = reversedJournalEntries; }
}
