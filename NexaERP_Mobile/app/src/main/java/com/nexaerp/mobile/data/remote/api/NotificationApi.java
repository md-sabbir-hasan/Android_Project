package com.nexaerp.mobile.data.remote.api;

import com.nexaerp.mobile.data.remote.model.ApiResponse;
import com.nexaerp.mobile.data.remote.model.PageResponse;
import com.nexaerp.mobile.data.remote.model.notification.NotificationItemResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NotificationApi {
    @GET("api/notifications/unread-count")
    Call<ApiResponse<Long>> getUnreadCount();

    @GET("api/notifications")
    Call<ApiResponse<PageResponse<NotificationItemResponse>>> getNotifications(
            @Query("page") int page,
            @Query("size") int size,
            @Query("unreadOnly") boolean unreadOnly
    );

    @PATCH("api/notifications/{id}/read")
    Call<ApiResponse<NotificationItemResponse>> markAsRead(@Path("id") long id);

    @PATCH("api/notifications/read-all")
    Call<ApiResponse<Void>> markAllAsRead();
}