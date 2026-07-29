package com.nexaerp.mobile.data.remote.model.dashboard;

public class UserSummaryResponse {
    private Long total;
    private Long active;
    private Long pending;
    private Long inactive;
    private Long locked;
    public UserSummaryResponse() {}
    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }
    public Long getActive() { return active; }
    public void setActive(Long active) { this.active = active; }
    public Long getPending() { return pending; }
    public void setPending(Long pending) { this.pending = pending; }
    public Long getInactive() { return inactive; }
    public void setInactive(Long inactive) { this.inactive = inactive; }
    public Long getLocked() { return locked; }
    public void setLocked(Long locked) { this.locked = locked; }
}
