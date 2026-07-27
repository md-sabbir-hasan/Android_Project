package com.nexaerp.mobile.data.remote.interceptor;

import com.nexaerp.mobile.data.local.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final TokenManager tokenManager;

    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String accessToken = tokenManager.getAccessToken();

        if (accessToken == null || accessToken.trim().isEmpty()) {
            return chain.proceed(request);
        }

        Request authenticatedRequest = request.newBuilder()
                .header("Authorization", "Bearer " + accessToken.trim())
                .build();
        return chain.proceed(authenticatedRequest);
    }
}