package com.nexaerp.mobile.data.remote.api;

import com.nexaerp.mobile.data.remote.model.ApiResponse;
import com.nexaerp.mobile.data.remote.model.PageResponse;
import com.nexaerp.mobile.data.remote.model.user.UserRequest;
import com.nexaerp.mobile.data.remote.model.user.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserApi {
    @GET("api/users")
    Call<ApiResponse<PageResponse<UserResponse>>> getAll(
            @Query("page") int page,
            @Query("size") int size,
            @Query("search") String search,
            @Query("status") String status
    );

    @GET("api/users/{id}")
    Call<ApiResponse<UserResponse>> getById(@Path("id") long id);

    @POST("api/users")
    Call<ApiResponse<UserResponse>> create(@Body UserRequest request);

    @PUT("api/users/{id}")
    Call<ApiResponse<UserResponse>> update(@Path("id") long id, @Body UserRequest request);

    @PATCH("api/users/{id}/activate")
    Call<ApiResponse<Void>> activate(@Path("id") long id);

    @PATCH("api/users/{id}/deactivate")
    Call<ApiResponse<Void>> deactivate(@Path("id") long id);
}