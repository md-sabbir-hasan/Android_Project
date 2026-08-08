package com.nexaerp.mobile.feature.user;

import android.content.Context;

import com.nexaerp.mobile.R;
import com.nexaerp.mobile.data.remote.model.user.UserResponse;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public final class UserPresenter {
    private static final int[] AVATAR_BG = {
            R.color.avatar_bg_1, R.color.avatar_bg_2, R.color.avatar_bg_3,
            R.color.avatar_bg_4, R.color.avatar_bg_5, R.color.avatar_bg_6
    };
    private static final int[] AVATAR_FG = {
            R.color.avatar_fg_1, R.color.avatar_fg_2, R.color.avatar_fg_3,
            R.color.avatar_fg_4, R.color.avatar_fg_5, R.color.avatar_fg_6
    };

    private UserPresenter() {}

    public static String safeName(UserResponse user, String fallback) {
        if (user == null || user.getName() == null || user.getName().trim().isEmpty()) {
            return fallback;
        }
        return user.getName().trim();
    }

    public static String initials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < parts.length && result.length() < 2; i++) {
            if (!parts[i].isEmpty()) {
                result.append(Character.toUpperCase(parts[i].charAt(0)));
            }
        }
        return result.length() == 0 ? "?" : result.toString();
    }

    /** Deterministic palette index so the same user always gets the same avatar color. */
    private static int paletteIndex(String seed) {
        if (seed == null || seed.isEmpty()) return 0;
        return Math.abs(seed.hashCode()) % AVATAR_BG.length;
    }

    public static int avatarBackgroundColorRes(String seed) {
        return AVATAR_BG[paletteIndex(seed)];
    }

    public static int avatarForegroundColorRes(String seed) {
        return AVATAR_FG[paletteIndex(seed)];
    }

    public static String statusLabel(Context context, String status) {
        if (status == null) return context.getString(R.string.user_status_unknown);
        switch (status) {
            case "ACTIVE": return context.getString(R.string.user_status_active);
            case "INACTIVE": return context.getString(R.string.user_status_inactive);
            case "LOCKED": return context.getString(R.string.user_status_locked);
            case "PENDING": return context.getString(R.string.user_status_pending);
            default: return status;
        }
    }

    public static int statusBackgroundColorRes(String status) {
        if (status == null) return R.color.status_inactive_bg;
        switch (status) {
            case "ACTIVE": return R.color.status_active_bg;
            case "LOCKED": return R.color.status_locked_bg;
            case "PENDING": return R.color.status_pending_bg;
            case "INACTIVE":
            default: return R.color.status_inactive_bg;
        }
    }

    public static int statusForegroundColorRes(String status) {
        if (status == null) return R.color.status_inactive_fg;
        switch (status) {
            case "ACTIVE": return R.color.status_active_fg;
            case "LOCKED": return R.color.status_locked_fg;
            case "PENDING": return R.color.status_pending_fg;
            case "INACTIVE":
            default: return R.color.status_inactive_fg;
        }
    }

    public static String formattedLastLogin(UserResponse user, String neverText) {
        if (user == null || user.getLastLoginAt() == null) {
            return neverText;
        }
        return user.getLastLoginAt().format(DateTimeFormatter.ofLocalizedDateTime(
                FormatStyle.MEDIUM, FormatStyle.SHORT
        ).withLocale(Locale.getDefault()));
    }

    public static String formattedCreatedAt(UserResponse user, String unavailableText) {
        if (user == null || user.getCreatedAt() == null) {
            return unavailableText;
        }
        return user.getCreatedAt().format(DateTimeFormatter.ofLocalizedDateTime(
                FormatStyle.MEDIUM, FormatStyle.SHORT
        ).withLocale(Locale.getDefault()));
    }
}