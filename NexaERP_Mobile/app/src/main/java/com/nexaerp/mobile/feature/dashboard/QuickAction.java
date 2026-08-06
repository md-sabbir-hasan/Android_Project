package com.nexaerp.mobile.feature.dashboard;

public final class QuickAction {
    private final String label;
    private final String permission;
    private final String route;

    public QuickAction(String label, String permission, String route) {
        this.label = label;
        this.permission = permission;
        this.route = route;
    }

    public String getLabel() { return label; }
    public String getPermission() { return permission; }
    public String getRoute() { return route; }
}