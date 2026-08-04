package com.nexaerp.mobile.feature.notification;

import com.nexaerp.mobile.data.remote.model.notification.NotificationItemResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NotificationUiState {
    private final boolean loading;
    private final boolean refreshing;
    private final boolean loadingMore;
    private final boolean markingAllAsRead;
    private final List<NotificationItemResponse> items;
    private final int page;
    private final boolean hasMore;
    private final boolean unreadOnly;
    private final String errorMessage;
    private final boolean retryable;
    private final String actionError;

    private NotificationUiState(
            boolean loading,
            boolean refreshing,
            boolean loadingMore,
            boolean markingAllAsRead,
            List<NotificationItemResponse> items,
            int page,
            boolean hasMore,
            boolean unreadOnly,
            String errorMessage,
            boolean retryable,
            String actionError
    ) {
        this.loading = loading;
        this.refreshing = refreshing;
        this.loadingMore = loadingMore;
        this.markingAllAsRead = markingAllAsRead;
        this.items = items;
        this.page = page;
        this.hasMore = hasMore;
        this.unreadOnly = unreadOnly;
        this.errorMessage = errorMessage;
        this.retryable = retryable;
        this.actionError = actionError;
    }

    public static NotificationUiState initialLoading(boolean unreadOnly) {
        return new NotificationUiState(
                true, false, false, false,
                Collections.emptyList(), 0, false, unreadOnly, null, false, null
        );
    }

    public NotificationUiState withRefreshing() {
        return new NotificationUiState(
                false, true, false, markingAllAsRead,
                items, page, hasMore, unreadOnly, null, false, null
        );
    }

    public NotificationUiState withLoadingMore() {
        return new NotificationUiState(
                loading, refreshing, true, markingAllAsRead,
                items, page, hasMore, unreadOnly, errorMessage, retryable, null
        );
    }

    /** Full-screen fatal error — only reachable when the list is still empty. */
    public NotificationUiState withFatalError(String message, boolean retryableFlag) {
        return new NotificationUiState(
                false, false, false, markingAllAsRead,
                items, page, hasMore, unreadOnly, message, retryableFlag, null
        );
    }

    /** A "load next page" failure — keep existing items on screen, surface a snackbar instead. */
    public NotificationUiState withLoadMoreError(String message) {
        return new NotificationUiState(
                false, false, false, markingAllAsRead,
                items, page, hasMore, unreadOnly, errorMessage, retryable, message
        );
    }

    public NotificationUiState withPage(
            List<NotificationItemResponse> newItems,
            int newPage,
            boolean append,
            boolean newHasMore,
            boolean unreadOnlyFlag
    ) {
        List<NotificationItemResponse> combined;
        if (append) {
            combined = new ArrayList<>(items);
            combined.addAll(newItems);
        } else {
            combined = new ArrayList<>(newItems);
        }
        return new NotificationUiState(
                false, false, false, markingAllAsRead,
                combined, newPage, newHasMore, unreadOnlyFlag, null, false, null
        );
    }

    public NotificationUiState withItemUpdated(NotificationItemResponse updated) {
        if (updated == null) {
            return this;
        }
        List<NotificationItemResponse> next = new ArrayList<>(items.size());
        for (NotificationItemResponse item : items) {
            boolean isMatch = item.getId() != null && item.getId().equals(updated.getId());
            next.add(isMatch ? updated : item);
        }
        return new NotificationUiState(
                loading, refreshing, loadingMore, markingAllAsRead,
                next, page, hasMore, unreadOnly, errorMessage, retryable, null
        );
    }

    public NotificationUiState withActionError(String message) {
        return new NotificationUiState(
                loading, refreshing, loadingMore, markingAllAsRead,
                items, page, hasMore, unreadOnly, errorMessage, retryable, message
        );
    }

    public NotificationUiState withMarkingAllAsRead(boolean marking) {
        return new NotificationUiState(
                loading, refreshing, loadingMore, marking,
                items, page, hasMore, unreadOnly, errorMessage, retryable, actionError
        );
    }

    public NotificationUiState withAllMarkedRead() {
        List<NotificationItemResponse> next = new ArrayList<>(items.size());
        for (NotificationItemResponse item : items) {
            item.setRead(true);
            next.add(item);
        }
        return new NotificationUiState(
                loading, refreshing, loadingMore, false,
                next, page, hasMore, unreadOnly, errorMessage, retryable, null
        );
    }

    public boolean isLoading() { return loading; }
    public boolean isRefreshing() { return refreshing; }
    public boolean isLoadingMore() { return loadingMore; }
    public boolean isMarkingAllAsRead() { return markingAllAsRead; }
    public List<NotificationItemResponse> getItems() { return items; }
    public int getPage() { return page; }
    public boolean hasMore() { return hasMore; }
    public boolean isUnreadOnly() { return unreadOnly; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isRetryable() { return retryable; }
    public String getActionError() { return actionError; }

    public boolean isFatalError() {
        return items.isEmpty() && errorMessage != null && !loading;
    }

    public boolean isEmptyResult() {
        return items.isEmpty() && errorMessage == null && !loading && !refreshing;
    }
}