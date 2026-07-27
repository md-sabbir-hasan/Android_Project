package com.nexaerp.mobile.data.remote.interceptor;

import com.nexaerp.mobile.data.local.SessionManager;
import com.nexaerp.mobile.data.local.TokenManager;
import com.nexaerp.mobile.data.remote.client.RefreshClient;
import com.nexaerp.mobile.data.remote.model.ApiResponse;
import com.nexaerp.mobile.data.remote.model.auth.LoginResponse;
import com.nexaerp.mobile.data.remote.model.auth.RefreshTokenRequest;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

public class TokenAuthenticator implements Authenticator {

    public static final class RetryMarker {
        public RetryMarker() {
        }
    }

    private final Object refreshLock = new Object();
    private final TokenManager tokenManager;

    public TokenAuthenticator(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public Request authenticate(Route route, Response response) {
        if (response.code() != 401 || responseCount(response) >= 2) {
            return null;
        }

        String failedToken = bearerToken(response.request());
        if (!refreshAfterAuthenticationFailure(failedToken)) {
            return null;
        }

        String newAccessToken = tokenManager.getAccessToken();
        if (isBlank(newAccessToken)) {
            return null;
        }

        return response.request().newBuilder()
                .header("Authorization", "Bearer " + newAccessToken.trim())
                .tag(RetryMarker.class, new RetryMarker())
                .build();
    }

    public boolean refreshIfNeeded() {
        if (!tokenManager.isAccessTokenExpiringSoon()) {
            return true;
        }
        return refreshAfterAuthenticationFailure(tokenManager.getAccessToken());
    }

    public boolean refreshAfterAuthenticationFailure(String failedToken) {
        synchronized (refreshLock) {
            String currentToken = tokenManager.getAccessToken();
            if (!isBlank(currentToken)
                    && !currentToken.equals(failedToken)
                    && !tokenManager.isAccessTokenExpiringSoon()) {
                return true;
            }

            String refreshToken = tokenManager.getRefreshToken();
            if (isBlank(refreshToken)) {
                expireSession();
                return false;
            }

            Call<ApiResponse<LoginResponse>> call = RefreshClient.getApiService()
                    .refreshToken(new RefreshTokenRequest(refreshToken));
            try {
                retrofit2.Response<ApiResponse<LoginResponse>> response = call.execute();
                if (response.isSuccessful()) {
                    ApiResponse<LoginResponse> body = response.body();
                    LoginResponse data = body == null ? null : body.getData();
                    if (body != null
                            && body.isSuccess()
                            && data != null
                            && !isBlank(data.getAccessToken())
                            && data.getExpiresIn() != null
                            && data.getExpiresIn() > 0L) {
                        tokenManager.updateAccessToken(data);
                        return true;
                    }
                    expireSession();
                    return false;
                }

                int code = response.code();
                if (code == 400 || code == 401 || code == 403) {
                    expireSession();
                }
                return false;
            } catch (IOException exception) {
                return false;
            }
        }
    }

    private void expireSession() {
        tokenManager.clearSession();
        SessionManager.getInstance().notifySessionExpired();
    }

    private int responseCount(Response response) {
        int count = 1;
        Response prior = response.priorResponse();
        while (prior != null) {
            count++;
            prior = prior.priorResponse();
        }
        return count;
    }

    private String bearerToken(Request request) {
        String authorization = request.header("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}