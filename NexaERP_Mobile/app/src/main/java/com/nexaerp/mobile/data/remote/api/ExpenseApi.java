package com.nexaerp.mobile.data.remote.api;

import com.nexaerp.mobile.data.remote.model.ApiResponse;
import com.nexaerp.mobile.data.remote.model.expense.ExpenseCancelRequest;
import com.nexaerp.mobile.data.remote.model.expense.ExpenseRequest;
import com.nexaerp.mobile.data.remote.model.expense.ExpenseResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ExpenseApi {
    @POST("api/expenses")
    Call<ApiResponse<ExpenseResponse>> create(@Body ExpenseRequest request);

    @GET("api/expenses/{id}")
    Call<ApiResponse<ExpenseResponse>> getById(@Path("id") long id);

    @GET("api/expenses")
    Call<ApiResponse<List<ExpenseResponse>>> getAll();

    @POST("api/expenses/{id}/cancel")
    Call<ApiResponse<ExpenseResponse>> cancel(@Path("id") long id, @Body ExpenseCancelRequest request);

    @POST("api/expenses/{id}/post")
    Call<ApiResponse<ExpenseResponse>> post(@Path("id") long id);
}