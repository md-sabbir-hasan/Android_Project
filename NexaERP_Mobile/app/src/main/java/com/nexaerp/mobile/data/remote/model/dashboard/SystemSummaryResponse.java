package com.nexaerp.mobile.data.remote.model.dashboard;

import java.time.LocalDateTime;

public class SystemSummaryResponse {
    private String applicationVersion;
    private LocalDateTime serverTime;
    private String serverTimezone;
    private String environment;
    private String javaVersion;
    public SystemSummaryResponse() {}
    public String getApplicationVersion() { return applicationVersion; }
    public void setApplicationVersion(String applicationVersion) { this.applicationVersion = applicationVersion; }
    public LocalDateTime getServerTime() { return serverTime; }
    public void setServerTime(LocalDateTime serverTime) { this.serverTime = serverTime; }
    public String getServerTimezone() { return serverTimezone; }
    public void setServerTimezone(String serverTimezone) { this.serverTimezone = serverTimezone; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getJavaVersion() { return javaVersion; }
    public void setJavaVersion(String javaVersion) { this.javaVersion = javaVersion; }
}
