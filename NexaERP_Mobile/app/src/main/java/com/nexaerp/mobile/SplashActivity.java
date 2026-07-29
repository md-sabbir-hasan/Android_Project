package com.nexaerp.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.nexaerp.mobile.data.local.TokenManager;
import com.nexaerp.mobile.data.remote.api.ApiService;
import com.nexaerp.mobile.data.remote.client.RefreshClient;
import com.nexaerp.mobile.data.remote.client.RetrofitClient;
import com.nexaerp.mobile.data.remote.model.ApiResponse;
import com.nexaerp.mobile.data.remote.model.auth.CurrentUserResponse;
import com.nexaerp.mobile.data.remote.model.auth.LoginResponse;
import com.nexaerp.mobile.data.remote.model.auth.RefreshTokenRequest;
import com.nexaerp.mobile.databinding.ActivitySplashBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private TokenManager tokenManager;
    private ApiService apiService;
    private boolean refreshAttempted;
    private Call<?> activeCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = new TokenManager(this);
        apiService = RetrofitClient.getApiService(getApplicationContext());
        binding.retryButton.setOnClickListener(view -> restoreSession());
        binding.goToLoginButton.setOnClickListener(view -> {
            tokenManager.clearSession();
            openLogin();
        });

        restoreSession();
    }

    private void restoreSession() {
        refreshAttempted = false;
        showLoading();

        if (!tokenManager.hasRefreshToken()) {
            openLogin();
            return;
        }

        if (!tokenManager.hasAccessToken() || tokenManager.isAccessTokenExpired()) {
            refreshSession();
            return;
        }

        verifyCurrentUser();
    }

    private void refreshSession() {
        String refreshToken = tokenManager.getRefreshToken();
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            tokenManager.clearSession();
            openLogin();
            return;
        }

        refreshAttempted = true;
        Call<ApiResponse<LoginResponse>> refreshCall = RefreshClient.getApiService()
                .refreshToken(new RefreshTokenRequest(refreshToken));
        activeCall = refreshCall;
        refreshCall.enqueue(new Callback<ApiResponse<LoginResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<LoginResponse>> call,
                            Response<ApiResponse<LoginResponse>> response
                    ) {
                        if (!response.isSuccessful()) {
                            int code = response.code();
                            if (code == 400 || code == 401 || code == 403) {
                                tokenManager.clearSession();
                                openLogin();
                            } else {
                                showError(getString(R.string.login_http_error, code));
                            }
                            return;
                        }

                        ApiResponse<LoginResponse> apiResponse = response.body();
                        LoginResponse data = apiResponse == null ? null : apiResponse.getData();
                        if (apiResponse == null
                                || !apiResponse.isSuccess()
                                || data == null
                                || data.getAccessToken() == null
                                || data.getAccessToken().trim().isEmpty()
                                || data.getExpiresIn() == null
                                || data.getExpiresIn() <= 0L) {
                            tokenManager.clearSession();
                            openLogin();
                            return;
                        }

                        tokenManager.updateAccessToken(data);
                        verifyCurrentUser();
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<LoginResponse>> call,
                            Throwable throwable
                    ) {
                        showError(messageOrFallback(throwable));
                    }
                });
    }

    private void verifyCurrentUser() {
        Call<ApiResponse<CurrentUserResponse>> currentUserCall = apiService.getCurrentUser();
        activeCall = currentUserCall;
        currentUserCall.enqueue(new Callback<ApiResponse<CurrentUserResponse>>() {
            @Override
            public void onResponse(
                    Call<ApiResponse<CurrentUserResponse>> call,
                    Response<ApiResponse<CurrentUserResponse>> response
            ) {
                if (response.isSuccessful()) {
                    ApiResponse<CurrentUserResponse> apiResponse = response.body();
                    if (apiResponse != null
                            && apiResponse.isSuccess()
                            && apiResponse.getData() != null) {
                        openMain();
                        return;
                    }

                    String message = apiResponse == null ? null : apiResponse.getMessage();
                    showError(message == null || message.trim().isEmpty()
                            ? getString(R.string.login_unexpected_response)
                            : message);
                    return;
                }

                int code = response.code();
                if ((code == 401 || code == 403) && !refreshAttempted) {
                    refreshSession();
                } else if (code == 401 || code == 403) {
                    tokenManager.clearSession();
                    openLogin();
                } else {
                    showError(getString(R.string.login_http_error, code));
                }
            }

            @Override
            public void onFailure(
                    Call<ApiResponse<CurrentUserResponse>> call,
                    Throwable throwable
            ) {
                showError(messageOrFallback(throwable));
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (activeCall != null) {
            activeCall.cancel();
        }
        super.onDestroy();
    }

    private void showLoading() {
        binding.loadingIndicator.setVisibility(View.VISIBLE);
        binding.errorMessage.setVisibility(View.GONE);
        binding.retryButton.setVisibility(View.GONE);
        binding.goToLoginButton.setVisibility(View.GONE);
    }

    private void showError(String message) {
        binding.loadingIndicator.setVisibility(View.GONE);
        binding.errorMessage.setText(message);
        binding.errorMessage.setVisibility(View.VISIBLE);
        binding.retryButton.setVisibility(View.VISIBLE);
        binding.goToLoginButton.setVisibility(View.VISIBLE);
    }

    private String messageOrFallback(Throwable throwable) {
        String message = throwable.getMessage();
        return message == null || message.trim().isEmpty()
                ? getString(R.string.login_network_error)
                : message;
    }

    private void openMain() {
        openAndClearTask(MainActivity.class);
    }

    private void openLogin() {
        openAndClearTask(LoginActivity.class);
    }

    private void openAndClearTask(Class<?> destination) {
        Intent intent = new Intent(this, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
