package com.nexaerp.mobile.data.remote.interceptor;

import com.nexaerp.mobile.data.local.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenRefreshInterceptor implements Interceptor {

    private final TokenManager tokenManager;
    private final TokenAuthenticator tokenAuthenticator;

    public TokenRefreshInterceptor(
            TokenManager tokenManager,
            TokenAuthenticator tokenAuthenticator
    ) {
        this.tokenManager = tokenManager;
        this.tokenAuthenticator = tokenAuthenticator;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (request.tag(TokenAuthenticator.RetryMarker.class) != null
                || isPublicAuthEndpoint(request)) {
            return chain.proceed(request);
        }

        if (tokenManager.hasRefreshToken() && tokenManager.isAccessTokenExpiringSoon()) {
            tokenAuthenticator.refreshIfNeeded();
            request = withCurrentToken(request);
        }

        return chain.proceed(request);
    }

    private Request withCurrentToken(Request request) {
        String accessToken = tokenManager.getAccessToken();
        Request.Builder builder = request.newBuilder();
        if (isBlank(accessToken)) {
            builder.removeHeader("Authorization");
        } else {
            builder.header("Authorization", "Bearer " + accessToken.trim());
        }
        return builder.build();
    }

    private boolean isPublicAuthEndpoint(Request request) {
        String path = request.url().encodedPath();
        return "/api/auth/login".equals(path) || "/api/auth/refresh".equals(path);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
