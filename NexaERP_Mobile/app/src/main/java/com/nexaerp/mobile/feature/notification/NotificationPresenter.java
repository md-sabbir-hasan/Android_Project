package com.nexaerp.mobile.feature.notification;

import com.nexaerp.mobile.data.remote.model.notification.NotificationItemResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public final class NotificationPresenter {
    private NotificationPresenter() {}

    public static String title(NotificationItemResponse item) {
        String title = item.getTitle();
        return title == null || title.trim().isEmpty() ? readableType(item.getType()) : title.trim();
    }

    public static String readableType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return "Notification";
        }
        String[] words = type.toLowerCase(Locale.ROOT).split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    public static String formatTimestamp(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "";
        }
        // Backend LocalDateTime has no offset. Formatting it as server-local wall time avoids
        // claiming a timezone conversion that the wire contract cannot support.
        return createdAt.format(DateTimeFormatter.ofLocalizedDateTime(
                FormatStyle.MEDIUM,
                FormatStyle.SHORT
        ).withLocale(Locale.getDefault()));
    }
}