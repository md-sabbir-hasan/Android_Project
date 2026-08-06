package com.nexaerp.mobile.data.remote.api;

import com.nexaerp.mobile.data.remote.model.ApiResponse;
import com.nexaerp.mobile.data.remote.model.role.RoleRequest;
import com.nexaerp.mobile.data.remote.model.role.RoleResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RoleApi {
    @GET("api/roles")
    Call<ApiResponse<List<RoleResponse>>> getAll();

    @GET("api/roles/{id}")
    Call<ApiResponse<RoleResponse>> getById(@Path("id") long id);

    @POST("api/roles")
    Call<ApiResponse<RoleResponse>> create(@Body RoleRequest request);

    @PUT("api/roles/{id}")
    Call<ApiResponse<RoleResponse>> update(@Path("id") long id, @Body RoleRequest request);
}