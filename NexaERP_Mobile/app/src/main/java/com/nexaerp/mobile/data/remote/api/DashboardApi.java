package com.nexaerp.mobile.data.remote.api;

import com.nexaerp.mobile.data.remote.model.ApiResponse;
import com.nexaerp.mobile.data.remote.model.dashboard.DashboardSummaryResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface DashboardApi {
    @GET("api/dashboard/summary")
    Call<ApiResponse<DashboardSummaryResponse>> getDashboardSummary();
}
