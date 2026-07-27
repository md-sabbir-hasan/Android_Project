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
        if (request.tag(TokenAuthenticator.RetryMarker.class) != null) {
            return chain.proceed(request);
        }

        if (tokenManager.hasRefreshToken() && tokenManager.isAccessTokenExpiringSoon()) {
            tokenAuthenticator.refreshIfNeeded();
            request = withCurrentToken(request);
        }

        Response response = chain.proceed(request);
        if (response.code() != 403 || !tokenManager.isAccessTokenExpired()) {
            return response;
        }

        String failedToken = bearerToken(request);
        if (!tokenAuthenticator.refreshAfterAuthenticationFailure(failedToken)) {
            return response;
        }

        String newAccessToken = tokenManager.getAccessToken();
        if (isBlank(newAccessToken)) {
            return response;
        }

        response.close();
        Request retry = request.newBuilder()
                .header("Authorization", "Bearer " + newAccessToken.trim())
                .tag(TokenAuthenticator.RetryMarker.class, new TokenAuthenticator.RetryMarker())
                .build();
        return chain.proceed(retry);
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

    private String bearerToken(Request request) {
        String authorization = request.header("Authorization");
        return authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring(7)
                : null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}