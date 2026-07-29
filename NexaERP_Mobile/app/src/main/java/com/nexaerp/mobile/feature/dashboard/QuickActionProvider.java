package com.nexaerp.mobile.feature.dashboard;

import com.nexaerp.mobile.core.permission.PermissionCodes;
import com.nexaerp.mobile.core.permission.PermissionEvaluator;

import java.util.ArrayList;
import java.util.List;

public final class QuickActionProvider {
    private static final QuickAction[] ACTIONS = {
            new QuickAction("New Invoice", PermissionCodes.CREATE_INVOICE),
            new QuickAction("New Expense", PermissionCodes.CREATE_EXPENSE),
            new QuickAction("New Journal", PermissionCodes.CREATE_JOURNAL),
            new QuickAction("New Payment", PermissionCodes.CREATE_PAYMENT),
            new QuickAction("New Vendor Bill", PermissionCodes.CREATE_VENDOR_BILL)
    };

    private QuickActionProvider() {}

    public static List<QuickAction> permitted(PermissionEvaluator evaluator) {
        List<QuickAction> result = new ArrayList<>();
        for (QuickAction action : ACTIONS) {
            if (evaluator.has(action.getPermission())) {
                result.add(action);
            }
        }
        return result;
    }
}
