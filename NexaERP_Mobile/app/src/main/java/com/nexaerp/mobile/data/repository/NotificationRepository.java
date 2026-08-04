package com.nexaerp.mobile.data.repository;

import androidx.annotation.NonNull;

import com.nexaerp.mobile.data.remote.api.NotificationApi;
import com.nexaerp.mobile.data.remote.model.ApiResponse;
import com.nexaerp.mobile.data.remote.model.PageResponse;
import com.nexaerp.mobile.data.remote.model.notification.NotificationItemResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class NotificationRepository {

    public interface ResultCallback {
        void onResult(Result result);
    }

    public interface ListResultCallback {
        void onResult(ListResult result);
    }

    public interface ItemResultCallback {
        void onResult(ItemResult result);
    }

    public interface ActionResultCallback {
        void onResult(ActionResult result);
    }

    /** Unread badge count. Unchanged shape — DashboardViewModel already depends on it. */
    public static final class Result {
        private final Long unreadCount;
        private final String errorMessage;

        private Result(Long unreadCount, String errorMessage) {
            this.unreadCount = unreadCount;
            this.errorMessage = errorMessage;
        }

        public static Result success(long unreadCount) {
            return new Result(Math.max(0L, unreadCount), null);
        }

        public static Result error(String errorMessage) {
            return new Result(null, errorMessage);
        }

        public boolean isSuccess() { return unreadCount != null; }
        public Long getUnreadCount() { return unreadCount; }
        public String getErrorMessage() { return errorMessage; }
    }

    /** A page of notifications for the list screen. */
    public static final class ListResult {
        private final PageResponse<NotificationItemResponse> page;
        private final String errorMessage;
        private final boolean retryable;

        private ListResult(
                PageResponse<NotificationItemResponse> page,
                String errorMessage,
                boolean retryable
        ) {
            this.page = page;
            this.errorMessage = errorMessage;
            this.retryable = retryable;
        }

        public static ListResult success(PageResponse<NotificationItemResponse> page) {
            return new ListResult(page, null, false);
        }

        public static ListResult error(String message, boolean retryable) {
            return new ListResult(null, message, retryable);
        }

        public boolean isSuccess() { return page != null; }
        public PageResponse<NotificationItemResponse> getPage() { return page; }
        public String getErrorMessage() { return errorMessage; }
        public boolean isRetryable() { return retryable; }
    }

    /** Result of marking a single notification as read. */
    public static final class ItemResult {
        private final NotificationItemResponse item;
        private final String errorMessage;

        private ItemResult(NotificationItemResponse item, String errorMessage) {
            this.item = item;
            this.errorMessage = errorMessage;
        }

        public static ItemResult success(NotificationItemResponse item) {
            return new ItemResult(item, null);
        }

        public static ItemResult error(String message) {
            return new ItemResult(null, message);
        }

        public boolean isSuccess() { return item != null; }
        public NotificationItemResponse getItem() { return item; }
        public String getErrorMessage() { return errorMessage; }
    }

    /** Result of "mark all as read", which has no response payload. */
    public static final class ActionResult {
        private final boolean success;
        private final String errorMessage;

        private ActionResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public static ActionResult success() { return new ActionResult(true, null); }
        public static ActionResult error(String message) { return new ActionResult(false, message); }

        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
    }

    private final NotificationApi notificationApi;
    private Call<ApiResponse<Long>> unreadCountCall;
    private Call<ApiResponse<PageResponse<NotificationItemResponse>>> listCall;
    private Call<ApiResponse<NotificationItemResponse>> markAsReadCall;
    private Call<ApiResponse<Void>> markAllAsReadCall;

    public NotificationRepository(NotificationApi notificationApi) {
        this.notificationApi = notificationApi;
    }

    public void loadUnreadCount(ResultCallback callback) {
        unreadCountCall = notificationApi.getUnreadCount();
        unreadCountCall.enqueue(new Callback<ApiResponse<Long>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<Long>> call,
                    @NonNull Response<ApiResponse<Long>> response
            ) {
                unreadCountCall = null;
                if (!response.isSuccessful()) {
                    callback.onResult(Result.error(
                            "Unable to load notification count (HTTP " + response.code() + ")."
                    ));
                    return;
                }
                callback.onResult(normalize(response.body()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<Long>> call,
                    @NonNull Throwable throwable
            ) {
                unreadCountCall = null;
                if (!call.isCanceled()) {
                    callback.onResult(Result.error("Unable to load notification count."));
                }
            }
        });
    }

    public void loadNotifications(
            int page,
            int size,
            boolean unreadOnly,
            ListResultCallback callback
    ) {
        listCall = notificationApi.getNotifications(page, size, unreadOnly);
        listCall.enqueue(new Callback<ApiResponse<PageResponse<NotificationItemResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<PageResponse<NotificationItemResponse>>> call,
                    @NonNull Response<ApiResponse<PageResponse<NotificationItemResponse>>> response
            ) {
                listCall = null;
                if (!response.isSuccessful()) {
                    callback.onResult(ListResult.error(
                            "Unable to load notifications (HTTP " + response.code() + ").",
                            response.code() >= 500 || response.code() == 408 || response.code() == 429
                    ));
                    return;
                }
                callback.onResult(normalizePage(response.body()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<PageResponse<NotificationItemResponse>>> call,
                    @NonNull Throwable throwable
            ) {
                listCall = null;
                if (!call.isCanceled()) {
                    callback.onResult(ListResult.error(
                            "Unable to connect. Check your connection and try again.",
                            true
                    ));
                }
            }
        });
    }

    public void markAsRead(long id, ItemResultCallback callback) {
        markAsReadCall = notificationApi.markAsRead(id);
        markAsReadCall.enqueue(new Callback<ApiResponse<NotificationItemResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<NotificationItemResponse>> call,
                    @NonNull Response<ApiResponse<NotificationItemResponse>> response
            ) {
                markAsReadCall = null;
                if (!response.isSuccessful()) {
                    callback.onResult(ItemResult.error(
                            "Unable to mark notification as read (HTTP " + response.code() + ")."
                    ));
                    return;
                }
                callback.onResult(normalizeItem(response.body()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<NotificationItemResponse>> call,
                    @NonNull Throwable throwable
            ) {
                markAsReadCall = null;
                if (!call.isCanceled()) {
                    callback.onResult(ItemResult.error(
                            "Unable to connect. Check your connection and try again."
                    ));
                }
            }
        });
    }

    public void markAllAsRead(ActionResultCallback callback) {
        markAllAsReadCall = notificationApi.markAllAsRead();
        markAllAsReadCall.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<Void>> call,
                    @NonNull Response<ApiResponse<Void>> response
            ) {
                markAllAsReadCall = null;
                if (!response.isSuccessful()) {
                    callback.onResult(ActionResult.error(
                            "Unable to mark all notifications as read (HTTP " + response.code() + ")."
                    ));
                    return;
                }
                ApiResponse<Void> body = response.body();
                if (body == null || !body.isSuccess()) {
                    String message = body == null ? null : body.getMessage();
                    callback.onResult(ActionResult.error(
                            message == null || message.trim().isEmpty()
                                    ? "Unable to mark all notifications as read."
                                    : message
                    ));
                    return;
                }
                callback.onResult(ActionResult.success());
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<Void>> call,
                    @NonNull Throwable throwable
            ) {
                markAllAsReadCall = null;
                if (!call.isCanceled()) {
                    callback.onResult(ActionResult.error(
                            "Unable to connect. Check your connection and try again."
                    ));
                }
            }
        });
    }

    public void cancel() {
        if (unreadCountCall != null) {
            unreadCountCall.cancel();
            unreadCountCall = null;
        }
        if (listCall != null) {
            listCall.cancel();
            listCall = null;
        }
        if (markAsReadCall != null) {
            markAsReadCall.cancel();
            markAsReadCall = null;
        }
        if (markAllAsReadCall != null) {
            markAllAsReadCall.cancel();
            markAllAsReadCall = null;
        }
    }

    static Result normalize(ApiResponse<Long> body) {
        if (body == null) {
            return Result.error("The server returned an empty notification response.");
        }
        if (!body.isSuccess()) {
            String message = body.getMessage();
            return Result.error(message == null || message.trim().isEmpty()
                    ? "The notification count could not be loaded."
                    : message);
        }
        if (body.getData() == null) {
            return Result.error("The server returned no notification count.");
        }
        return Result.success(body.getData());
    }

    static ListResult normalizePage(ApiResponse<PageResponse<NotificationItemResponse>> body) {
        if (body == null) {
            return ListResult.error("The server returned an empty response.", true);
        }
        if (!body.isSuccess()) {
            String message = body.getMessage();
            return ListResult.error(
                    message == null || message.trim().isEmpty()
                            ? "Notifications could not be loaded."
                            : message,
                    true
            );
        }
        if (body.getData() == null) {
            return ListResult.error("The server returned no notification data.", true);
        }
        return ListResult.success(body.getData());
    }

    static ItemResult normalizeItem(ApiResponse<NotificationItemResponse> body) {
        if (body == null) {
            return ItemResult.error("The server returned an empty response.");
        }
        if (!body.isSuccess() || body.getData() == null) {
            String message = body.getMessage();
            return ItemResult.error(message == null || message.trim().isEmpty()
                    ? "The notification could not be updated."
                    : message);
        }
        return ItemResult.success(body.getData());
    }
}