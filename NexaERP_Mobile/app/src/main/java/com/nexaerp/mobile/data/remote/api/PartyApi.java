package com.nexaerp.mobile.data.remote.api;

import com.nexaerp.mobile.data.remote.model.ApiResponse;
import com.nexaerp.mobile.data.remote.model.party.PartyResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface PartyApi {
    @GET("api/parties")
    Call<ApiResponse<List<PartyResponse>>> getAll();
}