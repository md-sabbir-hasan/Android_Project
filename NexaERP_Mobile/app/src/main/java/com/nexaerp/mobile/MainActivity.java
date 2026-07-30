package com.nexaerp.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.nexaerp.mobile.data.local.SessionManager;
import com.nexaerp.mobile.data.local.TokenManager;
import com.nexaerp.mobile.data.remote.api.ApiService;
import com.nexaerp.mobile.data.remote.client.RetrofitClient;
import com.nexaerp.mobile.data.remote.model.ApiResponse;
import com.nexaerp.mobile.data.remote.model.auth.CurrentUserResponse;
import com.nexaerp.mobile.databinding.ActivityMainBinding;
import com.nexaerp.mobile.feature.dashboard.DashboardFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements DashboardFragment.LogoutCallback {

    private ActivityMainBinding binding;
    private TokenManager tokenManager;
    private ApiService apiService;
    private Call<ApiResponse<CurrentUserResponse>> currentUserCall;
    private boolean logoutInProgress;
    private final SessionManager.Listener sessionListener = this::handleSessionExpired;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = new TokenManager(this);
        apiService = RetrofitClient.getApiService(getApplicationContext());

        binding.retryButton.setOnClickListener(view -> loadCurrentUser());
        loadCurrentUser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SessionManager.getInstance().registerListener(sessionListener);
    }

    @Override
    protected void onStop() {
        SessionManager.getInstance().unregisterListener(sessionListener);
        super.onStop();
    }

    private void loadCurrentUser() {
        showLoading();
        if (currentUserCall != null) {
            currentUserCall.cancel();
        }
        currentUserCall = apiService.getCurrentUser();
        currentUserCall.enqueue(new Callback<ApiResponse<CurrentUserResponse>>() {
            @Override
            public void onResponse(
                    Call<ApiResponse<CurrentUserResponse>> call,
                    Response<ApiResponse<CurrentUserResponse>> response
            ) {
                if (logoutInProgress || call.isCanceled()) {
                    return;
                }
                if (!response.isSuccessful()) {
                    showLoadFailure(getString(R.string.login_http_error, response.code()));
                    return;
                }

                ApiResponse<CurrentUserResponse> apiResponse = response.body();
                if (apiResponse != null
                        && apiResponse.isSuccess()
                        && apiResponse.getData() != null) {
                    displayVerifiedUser(apiResponse.getData());
                    showLoaded();
                    return;
                }

                String message = apiResponse == null ? null : apiResponse.getMessage();
                showLoadFailure(message == null || message.trim().isEmpty()
                        ? getString(R.string.login_unexpected_response)
                        : message);
            }

            @Override
            public void onFailure(
                    Call<ApiResponse<CurrentUserResponse>> call,
                    Throwable throwable
            ) {
                if (logoutInProgress || call.isCanceled()) {
                    return;
                }
                String message = throwable.getMessage();
                showLoadFailure(message == null || message.trim().isEmpty()
                        ? getString(R.string.login_network_error)
                        : message);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (currentUserCall != null) {
            currentUserCall.cancel();
        }
        super.onDestroy();
    }

    private void displayVerifiedUser(CurrentUserResponse currentUser) {
        if (getSupportFragmentManager().findFragmentByTag("dashboard") == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.dashboard_container,
                            DashboardFragment.newInstance(
                                    currentUser.getName(),
                                    currentUser.getRoles(),
                                    currentUser.getPermissions()
                            ),
                            "dashboard"
                    )
                    .commit();
        }
    }

    private void showLoading() {
        binding.verificationState.setVisibility(View.VISIBLE);
        binding.dashboardContainer.setVisibility(View.GONE);
        binding.loadingIndicator.setVisibility(View.VISIBLE);
        binding.retryButton.setVisibility(View.GONE);
    }

    private void showLoaded() {
        binding.verificationState.setVisibility(View.GONE);
        binding.dashboardContainer.setVisibility(View.VISIBLE);
    }

    private void showLoadFailure(String message) {
        binding.verificationState.setVisibility(View.VISIBLE);
        binding.dashboardContainer.setVisibility(View.GONE);
        binding.loadingIndicator.setVisibility(View.GONE);
        binding.retryButton.setVisibility(View.VISIBLE);
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onLogoutRequested() {
        if (logoutInProgress) {
            return;
        }
        logoutInProgress = true;

        if (currentUserCall != null) {
            currentUserCall.cancel();
            currentUserCall = null;
        }

        String accessToken = tokenManager.getAccessToken();
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            apiService.logout("Bearer " + accessToken.trim()).enqueue(
                    new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(
                                Call<ApiResponse<Void>> call,
                                Response<ApiResponse<Void>> response
                        ) {
                            // Local logout is intentionally independent of backend outcome.
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable throwable) {
                            // Local logout is intentionally independent of network availability.
                        }
                    }
            );
        }

        tokenManager.clearSession();
        openLoginAndClearTask();
    }

    private void handleSessionExpired() {
        if (logoutInProgress) {
            return;
        }
        logoutInProgress = true;
        if (currentUserCall != null) {
            currentUserCall.cancel();
            currentUserCall = null;
        }
        tokenManager.clearSession();
        openLoginAndClearTask();
    }

    private void openLoginAndClearTask() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
