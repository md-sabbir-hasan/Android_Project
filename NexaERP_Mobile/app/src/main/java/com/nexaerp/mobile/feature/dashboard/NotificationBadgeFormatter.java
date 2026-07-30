package com.nexaerp.mobile.feature.dashboard;

public final class NotificationBadgeFormatter {
    private NotificationBadgeFormatter() {}

    public static boolean isVisible(Long count) {
        return count != null && count > 0L;
    }

    public static String text(long count) {
        return count > 99L ? "99+" : Long.toString(Math.max(0L, count));
    }
}
