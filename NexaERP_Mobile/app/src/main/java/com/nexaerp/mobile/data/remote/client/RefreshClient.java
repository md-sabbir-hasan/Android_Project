package com.nexaerp.mobile.data.remote.client;

import com.nexaerp.mobile.BuildConfig;
import com.nexaerp.mobile.data.remote.api.ApiService;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RefreshClient {

    private static final ApiService API_SERVICE = createApiService();

    private RefreshClient() {
    }

    public static ApiService getApiService() {
        return API_SERVICE;
    }

    private static ApiService createApiService() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(SafeHttpLogging.create())
                .build();

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);
    }
}