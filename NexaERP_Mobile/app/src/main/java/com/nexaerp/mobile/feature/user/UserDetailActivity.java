package com.nexaerp.mobile.feature.user;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.nexaerp.mobile.R;
import com.nexaerp.mobile.data.remote.api.UserApi;
import com.nexaerp.mobile.data.remote.client.RetrofitClient;
import com.nexaerp.mobile.data.remote.model.user.UserResponse;
import com.nexaerp.mobile.data.repository.UserRepository;
import com.nexaerp.mobile.databinding.ActivityUserDetailBinding;

public class UserDetailActivity extends AppCompatActivity {

    private static final String EXTRA_USER_ID = "extra_user_id";

    private ActivityUserDetailBinding binding;
    private UserDetailViewModel viewModel;
    private long userId;
    private String lastShownTransientMessage;

    private final ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> viewModel.load()
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        finish();
                    }
                });

        userId = getIntent().getLongExtra(EXTRA_USER_ID, 0);

        UserApi userApi = RetrofitClient.createService(getApplicationContext(), UserApi.class);
        UserRepository repository = new UserRepository(userApi);
        viewModel = new ViewModelProvider(
                this, new UserDetailViewModelFactory(repository, userId)
        ).get(UserDetailViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finishWithResult());
        binding.toolbar.inflateMenu(R.menu.menu_user_detail);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_user) {
                editLauncher.launch(UserEditActivity.newIntent(this, userId));
                return true;
            }
            return false;
        });

        binding.retryButton.setOnClickListener(ignored -> viewModel.load());
        binding.statusToggleButton.setOnClickListener(ignored -> viewModel.toggleStatus());

        viewModel.getState().observe(this, this::render);
        viewModel.load();
    }

    private void render(UserDetailUiState state) {
        binding.loadingState.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
        boolean fatal = state.getErrorMessage() != null;
        binding.errorState.setVisibility(fatal ? View.VISIBLE : View.GONE);
        binding.content.setVisibility(!state.isLoading() && !fatal ? View.VISIBLE : View.GONE);

        if (fatal) {
            binding.errorMessage.setText(state.getErrorMessage());
            return;
        }
        if (state.getUser() == null) return;

        UserResponse user = state.getUser();
        String name = UserPresenter.safeName(user, getString(R.string.user_unnamed));

        binding.userName.setText(name);
        binding.userEmail.setText(user.getEmail());
        binding.userAvatarInitials.setText(UserPresenter.initials(name));

        if (binding.userAvatar.getBackground() instanceof GradientDrawable) {
            ((GradientDrawable) binding.userAvatar.getBackground().mutate()).setColor(
                    ContextCompat.getColor(this, UserPresenter.avatarBackgroundColorRes(name))
            );
        }
        binding.userAvatarInitials.setTextColor(
                ContextCompat.getColor(this, UserPresenter.avatarForegroundColorRes(name))
        );

        binding.userStatusPill.setText(UserPresenter.statusLabel(this, user.getStatus()));
        if (binding.userStatusPill.getBackground() instanceof GradientDrawable) {
            ((GradientDrawable) binding.userStatusPill.getBackground().mutate()).setColor(
                    ContextCompat.getColor(this, UserPresenter.statusBackgroundColorRes(user.getStatus()))
            );
        }
        binding.userStatusPill.setTextColor(
                ContextCompat.getColor(this, UserPresenter.statusForegroundColorRes(user.getStatus()))
        );

        boolean isActive = "ACTIVE".equals(user.getStatus());
        binding.statusToggleButton.setText(
                isActive ? R.string.user_deactivate : R.string.user_activate
        );
        binding.statusToggleButton.setEnabled(!state.isStatusUpdating());

        binding.lastLoginValue.setText(
                UserPresenter.formattedLastLogin(user, getString(R.string.user_never))
        );
        binding.createdAtValue.setText(
                UserPresenter.formattedCreatedAt(user, getString(R.string.notification_time_unavailable))
        );
        binding.failedAttemptsValue.setText(String.valueOf(
                user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()
        ));

        renderRoles(user);

        if (state.getTransientMessage() != null
                && !state.getTransientMessage().equals(lastShownTransientMessage)) {
            lastShownTransientMessage = state.getTransientMessage();
            Snackbar.make(binding.getRoot(), state.getTransientMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private void renderRoles(UserResponse user) {
        binding.roleChips.removeAllViews();
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            binding.rolesEmpty.setVisibility(View.VISIBLE);
            return;
        }
        binding.rolesEmpty.setVisibility(View.GONE);
        for (String role : user.getRoles()) {
            Chip chip = new Chip(this);
            chip.setText(role);
            chip.setClickable(false);
            chip.setCheckable(false);
            binding.roleChips.addView(chip);
        }
    }

    private void finishWithResult() {
        setResult(viewModel.hasChanges() ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
        finish();
    }


    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }

    public static Intent newIntent(Activity activity, long userId) {
        Intent intent = new Intent(activity, UserDetailActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        return intent;
    }
}