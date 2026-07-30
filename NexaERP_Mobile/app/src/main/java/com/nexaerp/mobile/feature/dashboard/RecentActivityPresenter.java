package com.nexaerp.mobile.feature.dashboard;

import com.nexaerp.mobile.core.permission.PermissionCodes;
import com.nexaerp.mobile.core.permission.PermissionEvaluator;
import com.nexaerp.mobile.data.remote.model.dashboard.RecentActivityResponse;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class RecentActivityPresenter {
    public static final int MAX_ITEMS = 5;

    private RecentActivityPresenter() {}

    public static boolean canShow(PermissionEvaluator evaluator) {
        return evaluator != null && evaluator.has(PermissionCodes.VIEW_AUDIT_LOGS);
    }

    public static List<RecentActivityResponse> visibleItems(
            PermissionEvaluator evaluator,
            List<RecentActivityResponse> activities
    ) {
        if (!canShow(evaluator) || activities == null || activities.isEmpty()) {
            return Collections.emptyList();
        }
        return activities.subList(0, Math.min(MAX_ITEMS, activities.size()));
    }

    public static String readableValue(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) return fallback;
        String normalized = value.trim().replace('_', ' ').toLowerCase(Locale.ROOT);
        StringBuilder result = new StringBuilder(normalized.length());
        boolean capitalize = true;
        for (int i = 0; i < normalized.length(); i++) {
            char character = normalized.charAt(i);
            if (capitalize && Character.isLetter(character)) {
                result.append(Character.toUpperCase(character));
                capitalize = false;
            } else {
                result.append(character);
            }
            if (character == ' ') capitalize = true;
        }
        return result.toString();
    }

    public static String safeTitle(RecentActivityResponse item) {
        if (item == null) return "Activity";
        String action = readableValue(item.getAction(), "Activity");
        String entity = readableValue(item.getEntityName(), "");
        return entity.isEmpty() ? action : action + " " + entity;
    }
}
