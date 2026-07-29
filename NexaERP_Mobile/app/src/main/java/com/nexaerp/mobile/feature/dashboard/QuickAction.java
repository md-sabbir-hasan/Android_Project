package com.nexaerp.mobile.feature.dashboard;

public final class QuickAction {
    private final String label;
    private final String permission;

    public QuickAction(String label, String permission) {
        this.label = label;
        this.permission = permission;
    }

    public String getLabel() { return label; }
    public String getPermission() { return permission; }
}
