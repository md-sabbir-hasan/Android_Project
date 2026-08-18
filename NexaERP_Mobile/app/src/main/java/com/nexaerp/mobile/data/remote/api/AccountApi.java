package com.nexaerp.mobile.data.remote.api;

import com.nexaerp.mobile.data.remote.model.ApiResponse;
import com.nexaerp.mobile.data.remote.model.account.AccountResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface AccountApi {
    @GET("api/accounts/type/{type}")
    Call<ApiResponse<List<AccountResponse>>> getByType(@Path("type") String type);
}