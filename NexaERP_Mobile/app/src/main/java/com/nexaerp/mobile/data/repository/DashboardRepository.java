package com.nexaerp.mobile.data.repository;

import androidx.annotation.NonNull;

import com.nexaerp.mobile.data.remote.api.DashboardApi;
import com.nexaerp.mobile.data.remote.model.ApiResponse;
import com.nexaerp.mobile.data.remote.model.dashboard.DashboardSummaryResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardRepository {
    public interface ResultCallback {
        void onResult(Result result);
    }

    public static final class Result {
        private final DashboardSummaryResponse data;
        private final String errorMessage;
        private final boolean retryable;

        private Result(DashboardSummaryResponse data, String errorMessage, boolean retryable) {
            this.data = data;
            this.errorMessage = errorMessage;
            this.retryable = retryable;
        }

        public static Result success(DashboardSummaryResponse data) {
            return new Result(data, null, false);
        }

        public static Result error(String message, boolean retryable) {
            return new Result(null, message, retryable);
        }

        public boolean isSuccess() { return data != null; }
        public DashboardSummaryResponse getData() { return data; }
        public String getErrorMessage() { return errorMessage; }
        public boolean isRetryable() { return retryable; }
    }

    private final DashboardApi dashboardApi;
    private Call<ApiResponse<DashboardSummaryResponse>> activeCall;

    public DashboardRepository(DashboardApi dashboardApi) {
        this.dashboardApi = dashboardApi;
    }

    public void loadDashboard(ResultCallback callback) {
        activeCall = dashboardApi.getDashboardSummary();
        activeCall.enqueue(new Callback<ApiResponse<DashboardSummaryResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<DashboardSummaryResponse>> call,
                    @NonNull Response<ApiResponse<DashboardSummaryResponse>> response
            ) {
                activeCall = null;
                if (!response.isSuccessful()) {
                    callback.onResult(Result.error(
                            "Unable to load dashboard (HTTP " + response.code() + ").",
                            response.code() >= 500 || response.code() == 408 || response.code() == 429
                    ));
                    return;
                }
                callback.onResult(normalize(response.body()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<DashboardSummaryResponse>> call,
                    @NonNull Throwable throwable
            ) {
                activeCall = null;
                if (call.isCanceled()) {
                    return;
                }
                callback.onResult(Result.error(
                        "Unable to connect. Check your connection and try again.",
                        true
                ));
            }
        });
    }

    public void cancel() {
        if (activeCall != null) {
            activeCall.cancel();
            activeCall = null;
        }
    }

    static Result normalize(ApiResponse<DashboardSummaryResponse> body) {
        if (body == null) {
            return Result.error("The server returned an empty response.", true);
        }
        if (!body.isSuccess()) {
            String message = body.getMessage();
            return Result.error(
                    message == null || message.trim().isEmpty()
                            ? "The dashboard could not be loaded."
                            : message,
                    true
            );
        }
        if (body.getData() == null) {
            return Result.error("The server returned no dashboard data.", true);
        }
        return Result.success(body.getData());
    }
}
