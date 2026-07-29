package com.nexaerp.mobile.feature.dashboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nexaerp.mobile.core.permission.PermissionCodes;
import com.nexaerp.mobile.core.permission.PermissionEvaluator;
import com.nexaerp.mobile.data.remote.model.dashboard.BusinessSummaryResponse;
import com.nexaerp.mobile.data.remote.model.dashboard.DashboardSummaryResponse;

import org.junit.Test;

import java.util.Set;

public class DashboardPermissionAndStateTest {
    @Test
    public void quickActionsContainOnlyExactGrantedCreatePermissions() {
        PermissionEvaluator evaluator = new PermissionEvaluator(Set.of(
                PermissionCodes.CREATE_INVOICE,
                PermissionCodes.CREATE_VENDOR_BILL,
                "VIEW_REPORT"
        ));

        assertEquals(2, QuickActionProvider.permitted(evaluator).size());
        assertEquals("New Invoice", QuickActionProvider.permitted(evaluator).get(0).getLabel());
        assertEquals("New Vendor Bill", QuickActionProvider.permitted(evaluator).get(1).getLabel());
    }

    @Test
    public void allNullSectionsAreAccessLimited() {
        DashboardSummaryResponse response = new DashboardSummaryResponse();
        assertTrue(DashboardUiState.content(response).isEmptyOrAccessLimited());

        response.setBusiness(new BusinessSummaryResponse());
        assertFalse(DashboardUiState.content(response).isEmptyOrAccessLimited());
    }
}
