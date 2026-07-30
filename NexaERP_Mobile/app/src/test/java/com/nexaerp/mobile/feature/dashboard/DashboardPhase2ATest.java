package com.nexaerp.mobile.feature.dashboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.nexaerp.mobile.core.permission.PermissionCodes;
import com.nexaerp.mobile.core.permission.PermissionEvaluator;
import com.nexaerp.mobile.data.remote.model.dashboard.DashboardSummaryResponse;
import com.nexaerp.mobile.data.remote.model.dashboard.RecentActivityResponse;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DashboardPhase2ATest {
    @Test
    public void badgeFormattingCoversBoundaries() {
        assertFalse(NotificationBadgeFormatter.isVisible(0L));
        assertEquals("0", NotificationBadgeFormatter.text(0L));
        assertEquals("1", NotificationBadgeFormatter.text(1L));
        assertEquals("99", NotificationBadgeFormatter.text(99L));
        assertEquals("99+", NotificationBadgeFormatter.text(100L));
    }

    @Test
    public void dashboardSuccessSurvivesUnreadFailure() {
        DashboardSummaryResponse dashboard = new DashboardSummaryResponse();
        DashboardUiState state = DashboardUiState.content(dashboard)
                .withUnreadError("Count failed");

        assertEquals(dashboard, state.getData());
        assertEquals("Count failed", state.getUnreadCountError());
        assertNull(state.getErrorMessage());
    }

    @Test
    public void unreadSuccessDoesNotHideDashboardFailure() {
        DashboardUiState state = DashboardUiState.fatalError("Dashboard failed", true)
                .withUnreadCount(4L);

        assertEquals("Dashboard failed", state.getErrorMessage());
        assertEquals(Long.valueOf(4L), state.getUnreadCount());
        assertNull(state.getData());
    }

    @Test
    public void recentActivityRequiresPermissionAndLimitsToFive() {
        List<RecentActivityResponse> activities = activities(7);
        PermissionEvaluator denied = new PermissionEvaluator(Collections.emptySet());
        PermissionEvaluator allowed = new PermissionEvaluator(
                Set.of(PermissionCodes.VIEW_AUDIT_LOGS)
        );

        assertTrue(RecentActivityPresenter.visibleItems(denied, activities).isEmpty());
        assertEquals(5, RecentActivityPresenter.visibleItems(allowed, activities).size());
    }

    @Test
    public void recentActivityHandlesNullAndEmpty() {
        PermissionEvaluator allowed = new PermissionEvaluator(
                Set.of(PermissionCodes.VIEW_AUDIT_LOGS)
        );

        assertTrue(RecentActivityPresenter.visibleItems(allowed, null).isEmpty());
        assertTrue(RecentActivityPresenter.visibleItems(
                allowed,
                Collections.emptyList()
        ).isEmpty());
    }

    @Test
    public void unknownAndMissingActivityValuesHaveSafeReadableFallbacks() {
        RecentActivityResponse unknown = new RecentActivityResponse();
        unknown.setAction("ARCHIVED_EXTERNALLY");
        unknown.setEntityName("CUSTOM_RECORD");
        assertEquals(
                "Archived Externally Custom Record",
                RecentActivityPresenter.safeTitle(unknown)
        );
        assertEquals("Activity", RecentActivityPresenter.safeTitle(null));

        RecentActivityResponse missing = new RecentActivityResponse();
        assertEquals("Activity", RecentActivityPresenter.safeTitle(missing));
    }

    private List<RecentActivityResponse> activities(int count) {
        List<RecentActivityResponse> result = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            RecentActivityResponse item = new RecentActivityResponse();
            item.setAction("UPDATE");
            item.setEntityName("INVOICE");
            item.setEntityId((long) index);
            result.add(item);
        }
        return result;
    }
}
