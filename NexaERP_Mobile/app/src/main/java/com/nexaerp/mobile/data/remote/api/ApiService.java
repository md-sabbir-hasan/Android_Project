package com.nexaerp.mobile.data.remote.api;

import com.nexaerp.mobile.data.remote.model.ApiResponse;
import com.nexaerp.mobile.data.remote.model.auth.CurrentUserResponse;
import com.nexaerp.mobile.data.remote.model.auth.LoginRequest;
import com.nexaerp.mobile.data.remote.model.auth.LoginResponse;
import com.nexaerp.mobile.data.remote.model.auth.RefreshTokenRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest request);

    @POST("api/auth/refresh")
    Call<ApiResponse<LoginResponse>> refreshToken(@Body RefreshTokenRequest request);

    @GET("api/auth/me")
    Call<ApiResponse<CurrentUserResponse>> getCurrentUser();

    @POST("api/auth/logout")
    Call<ApiResponse<Void>> logout(@Header("Authorization") String authorization);
}