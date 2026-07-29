package com.nexaerp.mobile.data.remote.client;

import android.content.Context;

import com.nexaerp.mobile.BuildConfig;
import com.nexaerp.mobile.data.local.TokenManager;
import com.nexaerp.mobile.data.remote.api.ApiService;
import com.nexaerp.mobile.data.remote.interceptor.AuthInterceptor;
import com.nexaerp.mobile.data.remote.interceptor.TokenAuthenticator;
import com.nexaerp.mobile.data.remote.interceptor.TokenRefreshInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {

    private static volatile Retrofit retrofit;
    private static volatile ApiService apiService;
    private static volatile TokenAuthenticator tokenAuthenticator;

    private RetrofitClient() {
    }

    public static ApiService getApiService(Context context) {
        ensureInitialized(context);
        return apiService;
    }

    public static <T> T createService(Context context, Class<T> serviceClass) {
        ensureInitialized(context);
        return retrofit.create(serviceClass);
    }

    public static TokenAuthenticator getTokenAuthenticator(Context context) {
        ensureInitialized(context);
        return tokenAuthenticator;
    }

    private static void ensureInitialized(Context context) {
        if (apiService != null) {
            return;
        }

        synchronized (RetrofitClient.class) {
            if (apiService != null) {
                return;
            }

            Context applicationContext = context.getApplicationContext();
            TokenManager tokenManager = new TokenManager(applicationContext);
            tokenAuthenticator = new TokenAuthenticator(tokenManager);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(SafeHttpLogging.create())
                    .addInterceptor(new AuthInterceptor(tokenManager))
                    .addInterceptor(new TokenRefreshInterceptor(
                            tokenManager,
                            tokenAuthenticator
                    ))
                    .authenticator(tokenAuthenticator)
                    .build();

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            apiService = retrofit.create(ApiService.class);
        }
    }
}
