package com.nexaerp.mobile.data.repository;

import androidx.annotation.NonNull;

import com.nexaerp.mobile.data.remote.api.NotificationApi;
import com.nexaerp.mobile.data.remote.model.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class NotificationRepository {
    public interface ResultCallback {
        void onResult(Result result);
    }

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

    private final NotificationApi notificationApi;
    private Call<ApiResponse<Long>> activeCall;

    public NotificationRepository(NotificationApi notificationApi) {
        this.notificationApi = notificationApi;
    }

    public void loadUnreadCount(ResultCallback callback) {
        activeCall = notificationApi.getUnreadCount();
        activeCall.enqueue(new Callback<ApiResponse<Long>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<Long>> call,
                    @NonNull Response<ApiResponse<Long>> response
            ) {
                activeCall = null;
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
                activeCall = null;
                if (!call.isCanceled()) {
                    callback.onResult(Result.error("Unable to load notification count."));
                }
            }
        });
    }

    public void cancel() {
        if (activeCall != null) {
            activeCall.cancel();
            activeCall = null;
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
}
