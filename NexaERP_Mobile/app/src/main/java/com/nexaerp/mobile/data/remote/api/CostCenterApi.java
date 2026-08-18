package com.nexaerp.mobile.data.remote.api;

import com.nexaerp.mobile.data.remote.model.ApiResponse;
import com.nexaerp.mobile.data.remote.model.costcenter.CostCenterResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CostCenterApi {
    @GET("api/cost-centers")
    Call<ApiResponse<List<CostCenterResponse>>> getAll();
}