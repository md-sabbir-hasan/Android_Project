package com.nexaerp.mobile.feature.dashboard;

import com.nexaerp.mobile.core.permission.PermissionCodes;
import com.nexaerp.mobile.core.permission.PermissionEvaluator;

import java.util.ArrayList;
import java.util.List;

public final class QuickActionProvider {
    public static final String ROUTE_NEW_INVOICE = "NEW_INVOICE";
    public static final String ROUTE_NEW_EXPENSE = "NEW_EXPENSE";
    public static final String ROUTE_NEW_JOURNAL = "NEW_JOURNAL";
    public static final String ROUTE_NEW_PAYMENT = "NEW_PAYMENT";
    public static final String ROUTE_NEW_VENDOR_BILL = "NEW_VENDOR_BILL";
    public static final String ROUTE_MANAGE_USERS = "MANAGE_USERS";
    public static final String ROUTE_MANAGE_ROLES = "MANAGE_ROLES";

    private static final QuickAction[] ACTIONS = {
            new QuickAction("Manage Users", PermissionCodes.MANAGE_USERS, ROUTE_MANAGE_USERS),
            new QuickAction("Manage Roles", PermissionCodes.MANAGE_ROLES, ROUTE_MANAGE_ROLES),
            new QuickAction("New Expense", PermissionCodes.CREATE_EXPENSE, ROUTE_NEW_EXPENSE),
            new QuickAction("New Invoice", PermissionCodes.CREATE_INVOICE, ROUTE_NEW_INVOICE),
            new QuickAction("New Journal", PermissionCodes.CREATE_JOURNAL, ROUTE_NEW_JOURNAL),
            new QuickAction("New Payment", PermissionCodes.CREATE_PAYMENT, ROUTE_NEW_PAYMENT),
            new QuickAction("New Vendor Bill", PermissionCodes.CREATE_VENDOR_BILL, ROUTE_NEW_VENDOR_BILL)
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