package com.nexaerp.mobile.data.remote.model.dashboard;

public class SecuritySummaryResponse {
    private Long totalRoles;
    private Long totalPermissions;
    public SecuritySummaryResponse() {}
    public Long getTotalRoles() { return totalRoles; }
    public void setTotalRoles(Long totalRoles) { this.totalRoles = totalRoles; }
    public Long getTotalPermissions() { return totalPermissions; }
    public void setTotalPermissions(Long totalPermissions) { this.totalPermissions = totalPermissions; }
}
