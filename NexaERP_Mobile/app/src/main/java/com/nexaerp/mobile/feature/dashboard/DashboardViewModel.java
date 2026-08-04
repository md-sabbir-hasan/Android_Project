package com.nexaerp.mobile.feature.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nexaerp.mobile.data.remote.model.dashboard.DashboardSummaryResponse;
import com.nexaerp.mobile.data.repository.DashboardRepository;
import com.nexaerp.mobile.data.repository.NotificationRepository;

public class DashboardViewModel extends ViewModel {
    private static final long UNREAD_REFRESH_INTERVAL_MS = 60_000L;
    private final DashboardRepository dashboardRepository;
    private final NotificationRepository notificationRepository;
    private final MutableLiveData<DashboardUiState> state =
            new MutableLiveData<>(DashboardUiState.initialLoading());
    private boolean requestInFlight;
    private boolean unreadRequestInFlight;
    private long lastUnreadSuccessAt;

    public DashboardViewModel(
            DashboardRepository dashboardRepository,
            NotificationRepository notificationRepository
    ) {
        this.dashboardRepository = dashboardRepository;
        this.notificationRepository = notificationRepository;
    }

    public LiveData<DashboardUiState> getState() {
        return state;
    }

    public void loadDashboard() {
        DashboardUiState current = state.getValue();
        if (requestInFlight || (current != null && current.getData() != null)) {
            return;
        }
        request(false);
        requestUnreadCount(true);
    }

    public void refreshDashboard() {
        if (requestInFlight) {
            return;
        }
        request(true);
        requestUnreadCount(true);
    }

    public void retry() {
        if (requestInFlight) {
            return;
        }
        request(false);
        requestUnreadCount(true);
    }

    public void refreshUnreadCountIfStale() {
        requestUnreadCount(false);
    }

    /** Used after returning from the Notification Center, where the count may have changed. */
    public void forceRefreshUnreadCount() {
        requestUnreadCount(true);
    }

    private void request(boolean refresh) {
        requestInFlight = true;
        DashboardUiState current = state.getValue();
        DashboardSummaryResponse retained = current == null ? null : current.getData();
        DashboardUiState dashboardLoading = refresh && retained != null
                ? DashboardUiState.refreshing(retained)
                : DashboardUiState.initialLoading();
        state.setValue(dashboardLoading.preservingUnreadFrom(current));

        dashboardRepository.loadDashboard(result -> {
            requestInFlight = false;
            DashboardUiState beforeResult = state.getValue();
            DashboardUiState next;
            if (result.isSuccess()) {
                next = DashboardUiState.content(result.getData());
            } else if (retained != null) {
                next = DashboardUiState.contentWithError(
                        retained,
                        result.getErrorMessage(),
                        result.isRetryable()
                );
            } else {
                next = DashboardUiState.fatalError(
                        result.getErrorMessage(),
                        result.isRetryable()
                );
            }
            state.setValue(next.preservingUnreadFrom(beforeResult));
        });
    }

    private void requestUnreadCount(boolean force) {
        long now = System.currentTimeMillis();
        if (unreadRequestInFlight
                || (!force && lastUnreadSuccessAt > 0L
                && now - lastUnreadSuccessAt < UNREAD_REFRESH_INTERVAL_MS)) {
            return;
        }
        unreadRequestInFlight = true;
        DashboardUiState current = state.getValue();
        if (current != null) state.setValue(current.withUnreadLoading());
        notificationRepository.loadUnreadCount(result -> {
            unreadRequestInFlight = false;
            DashboardUiState latest = state.getValue();
            if (latest == null) return;
            if (result.isSuccess()) {
                lastUnreadSuccessAt = System.currentTimeMillis();
                state.setValue(latest.withUnreadCount(result.getUnreadCount()));
            } else {
                state.setValue(latest.withUnreadError(result.getErrorMessage()));
            }
        });
    }

    @Override
    protected void onCleared() {
        dashboardRepository.cancel();
        notificationRepository.cancel();
    }
}
