package com.nexaerp.mobile.data.remote.api;

import com.nexaerp.mobile.data.remote.model.ApiResponse;
import com.nexaerp.mobile.data.remote.model.role.PermissionResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface PermissionApi {
    @GET("api/permissions")
    Call<ApiResponse<List<PermissionResponse>>> getAll();
}