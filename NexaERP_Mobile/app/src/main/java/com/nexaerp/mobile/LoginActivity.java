package com.nexaerp.mobile;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.nexaerp.mobile.data.local.TokenManager;
import com.nexaerp.mobile.data.remote.api.ApiService;
import com.nexaerp.mobile.data.remote.client.RetrofitClient;
import com.nexaerp.mobile.data.remote.model.ApiResponse;
import com.nexaerp.mobile.data.remote.model.auth.LoginRequest;
import com.nexaerp.mobile.data.remote.model.auth.LoginResponse;
import com.nexaerp.mobile.databinding.ActivityLoginBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private ApiService apiService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getApiService(getApplicationContext());
        tokenManager = new TokenManager(this);
        binding.signInButton.setOnClickListener(view -> attemptLogin());
    }

    private void attemptLogin() {
        binding.emailInputLayout.setError(null);
        binding.passwordInputLayout.setError(null);

        String email = binding.emailInput.getText() == null
                ? ""
                : binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText() == null
                ? ""
                : binding.passwordInput.getText().toString();

        boolean isValid = true;

        if (email.isEmpty()) {
            binding.emailInputLayout.setError(getString(R.string.login_email_required));
            isValid = false;
        }

        if (password.isEmpty()) {
            binding.passwordInputLayout.setError(getString(R.string.login_password_required));
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        setLoading(true);
        apiService.login(new LoginRequest(email, password)).enqueue(
                new Callback<ApiResponse<LoginResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<LoginResponse>> call,
                            Response<ApiResponse<LoginResponse>> response
                    ) {
                        setLoading(false);

                        if (!response.isSuccessful()) {
                            showSnackbar(getString(R.string.login_http_error, response.code()));
                            return;
                        }

                        ApiResponse<LoginResponse> apiResponse = response.body();
                        if (apiResponse != null && apiResponse.isSuccess()) {
                            LoginResponse loginResponse = apiResponse.getData();
                            if (loginResponse == null) {
                                showSnackbar(getString(R.string.login_unexpected_response));
                                return;
                            }

                            if (loginResponse.getAccessToken() == null
                                    || loginResponse.getAccessToken().trim().isEmpty()
                                    || loginResponse.getRefreshToken() == null
                                    || loginResponse.getRefreshToken().trim().isEmpty()
                                    || loginResponse.getExpiresIn() == null
                                    || loginResponse.getExpiresIn() <= 0L) {
                                showSnackbar(getString(R.string.login_unexpected_response));
                                return;
                            }

                            tokenManager.saveLoginSession(loginResponse);
                            if (!tokenManager.hasAccessToken()
                                    || !tokenManager.hasRefreshToken()
                                    || tokenManager.getAccessTokenExpiresAt() <= 0L) {
                                showSnackbar(getString(R.string.login_session_save_failed));
                                return;
                            }

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            return;
                        }

                        String message = apiResponse == null ? null : apiResponse.getMessage();
                        showSnackbar(message == null || message.trim().isEmpty()
                                ? getString(R.string.login_unexpected_response)
                                : message);
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<LoginResponse>> call,
                            Throwable throwable
                    ) {
                        setLoading(false);
                        String message = throwable.getMessage();
                        showSnackbar(message == null || message.trim().isEmpty()
                                ? getString(R.string.login_network_error)
                                : message);
                    }
                }
        );
    }

    private void setLoading(boolean isLoading) {
        binding.signInButton.setEnabled(!isLoading);
        binding.signInButton.setText(isLoading
                ? R.string.login_signing_in
                : R.string.login_sign_in);
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }
}