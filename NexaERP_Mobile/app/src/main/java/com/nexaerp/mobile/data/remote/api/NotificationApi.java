package com.nexaerp.mobile.data.remote.api;

import com.nexaerp.mobile.data.remote.model.ApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface NotificationApi {
    @GET("api/notifications/unread-count")
    Call<ApiResponse<Long>> getUnreadCount();
}
