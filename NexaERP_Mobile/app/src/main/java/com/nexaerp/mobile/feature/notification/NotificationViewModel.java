package com.nexaerp.mobile.feature.notification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nexaerp.mobile.data.remote.model.PageResponse;
import com.nexaerp.mobile.data.remote.model.notification.NotificationItemResponse;
import com.nexaerp.mobile.data.repository.NotificationRepository;

public class NotificationViewModel extends ViewModel {
    private static final int PAGE_SIZE = 20;

    private final NotificationRepository repository;
    private final MutableLiveData<NotificationUiState> state =
            new MutableLiveData<>(NotificationUiState.initialLoading(false));

    private boolean requestInFlight;
    private boolean unreadCountDirty;

    public NotificationViewModel(NotificationRepository repository) {
        this.repository = repository;
    }

    public LiveData<NotificationUiState> getState() {
        return state;
    }

    /** True once at least one notification has been marked read during this screen visit. */
    public boolean isUnreadCountDirty() {
        return unreadCountDirty;
    }

    public void loadFirstPage() {
        NotificationUiState current = state.getValue();
        if (requestInFlight || (current != null && !current.getItems().isEmpty())) {
            return;
        }
        request(0, false, current != null && current.isUnreadOnly());
    }

    public void refresh() {
        if (requestInFlight) {
            return;
        }
        NotificationUiState current = state.getValue();
        boolean unreadOnly = current != null && current.isUnreadOnly();
        if (current != null) {
            state.setValue(current.withRefreshing());
        }
        request(0, false, unreadOnly);
    }

    public void retry() {
        if (requestInFlight) {
            return;
        }
        NotificationUiState current = state.getValue();
        boolean unreadOnly = current != null && current.isUnreadOnly();
        state.setValue(NotificationUiState.initialLoading(unreadOnly));
        request(0, false, unreadOnly);
    }

    public void loadNextPage() {
        NotificationUiState current = state.getValue();
        if (requestInFlight || current == null || !current.hasMore()
                || current.isLoading() || current.isLoadingMore()) {
            return;
        }
        state.setValue(current.withLoadingMore());
        request(current.getPage() + 1, true, current.isUnreadOnly());
    }

    public void setUnreadOnly(boolean unreadOnly) {
        NotificationUiState current = state.getValue();
        if (requestInFlight || current == null || current.isUnreadOnly() == unreadOnly) {
            return;
        }
        state.setValue(NotificationUiState.initialLoading(unreadOnly));
        request(0, false, unreadOnly);
    }

    private void request(int page, boolean append, boolean unreadOnly) {
        requestInFlight = true;
        repository.loadNotifications(page, PAGE_SIZE, unreadOnly, result -> {
            requestInFlight = false;
            NotificationUiState current = state.getValue();
            if (current == null) {
                return;
            }
            if (!result.isSuccess()) {
                state.setValue(append
                        ? current.withLoadMoreError(result.getErrorMessage())
                        : current.withFatalError(result.getErrorMessage(), result.isRetryable()));
                return;
            }
            PageResponse<NotificationItemResponse> pageData = result.getPage();
            state.setValue(current.withPage(
                    pageData.getContent(),
                    pageData.getPage(),
                    append,
                    !pageData.isLast(),
                    unreadOnly
            ));
        });
    }

    public void toggleRead(NotificationItemResponse item) {
        if (item == null || item.getId() == null || item.isRead()) {
            return;
        }
        repository.markAsRead(item.getId(), result -> {
            NotificationUiState latest = state.getValue();
            if (latest == null) {
                return;
            }
            if (result.isSuccess()) {
                unreadCountDirty = true;
                state.setValue(latest.withItemUpdated(result.getItem()));
            } else {
                state.setValue(latest.withActionError(result.getErrorMessage()));
            }
        });
    }

    public void markAllAsRead() {
        NotificationUiState current = state.getValue();
        if (current == null || current.isMarkingAllAsRead()) {
            return;
        }
        state.setValue(current.withMarkingAllAsRead(true));
        repository.markAllAsRead(result -> {
            NotificationUiState latest = state.getValue();
            if (latest == null) {
                return;
            }
            if (result.isSuccess()) {
                unreadCountDirty = true;
                state.setValue(latest.withAllMarkedRead());
            } else {
                state.setValue(latest.withMarkingAllAsRead(false).withActionError(result.getErrorMessage()));
            }
        });
    }

    @Override
    protected void onCleared() {
        repository.cancel();
    }
}