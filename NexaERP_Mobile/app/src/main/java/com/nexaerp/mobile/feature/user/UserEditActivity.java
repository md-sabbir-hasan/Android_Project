package com.nexaerp.mobile.feature.user;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.nexaerp.mobile.R;
import com.nexaerp.mobile.data.remote.api.PermissionApi;
import com.nexaerp.mobile.data.remote.api.RoleApi;
import com.nexaerp.mobile.data.remote.api.UserApi;
import com.nexaerp.mobile.data.remote.client.RetrofitClient;
import com.nexaerp.mobile.data.remote.model.role.RoleResponse;
import com.nexaerp.mobile.data.repository.RoleRepository;
import com.nexaerp.mobile.data.repository.UserRepository;
import com.nexaerp.mobile.databinding.ActivityUserEditBinding;

public class UserEditActivity extends AppCompatActivity {

    private static final String EXTRA_USER_ID = "extra_user_id";

    private ActivityUserEditBinding binding;
    private UserEditViewModel viewModel;
    private boolean roleViewsBuilt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        long userId = getIntent().getLongExtra(EXTRA_USER_ID, 0);

        UserApi userApi = RetrofitClient.createService(getApplicationContext(), UserApi.class);
        RoleApi roleApi = RetrofitClient.createService(getApplicationContext(), RoleApi.class);
        PermissionApi permissionApi = RetrofitClient.createService(
                getApplicationContext(), PermissionApi.class
        );
        UserRepository userRepository = new UserRepository(userApi);
        RoleRepository roleRepository = new RoleRepository(roleApi, permissionApi);

        viewModel = new ViewModelProvider(
                this, new UserEditViewModelFactory(userRepository, roleRepository, userId)
        ).get(UserEditViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.setTitle(
                viewModel.isEditMode() ? R.string.user_edit_title : R.string.user_create_title
        );

        binding.nameInput.setOnFocusChangeListener((v, hasFocus) -> syncFieldsToViewModel());
        binding.emailInput.setOnFocusChangeListener((v, hasFocus) -> syncFieldsToViewModel());
        binding.saveButton.setOnClickListener(ignored -> {
            syncFieldsToViewModel();
            viewModel.save();
        });
        binding.retryButton.setOnClickListener(ignored -> viewModel.load());

        if (viewModel.isEditMode()) {
            binding.emailInput.setEnabled(false);
            binding.emailHint.setVisibility(View.VISIBLE);
        }

        viewModel.getState().observe(this, this::render);
        viewModel.load();
    }

    private void syncFieldsToViewModel() {
        viewModel.setFields(
                binding.nameInput.getText() == null ? "" : binding.nameInput.getText().toString(),
                binding.emailInput.getText() == null ? "" : binding.emailInput.getText().toString()
        );
    }

    private void render(UserEditUiState state) {
        boolean fatal = state.getErrorMessage() != null && state.getRoles().isEmpty();
        binding.loadingState.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
        binding.errorState.setVisibility(fatal ? View.VISIBLE : View.GONE);
        binding.formContent.setVisibility(!state.isLoading() && !fatal ? View.VISIBLE : View.GONE);
        if (fatal) {
            binding.errorMessage.setText(state.getErrorMessage());
            return;
        }
        if (state.isLoading()) return;

        if (!roleViewsBuilt) {
            binding.nameInput.setText(state.getName());
            binding.emailInput.setText(state.getEmail());
            buildRoleChecklist(state);
            roleViewsBuilt = true;
        }

        binding.saveButton.setEnabled(!state.isSaving());
        binding.saveButton.setText(state.isSaving() ? R.string.role_saving : R.string.role_save);

        if (state.getErrorMessage() != null && !state.getRoles().isEmpty()) {
            Snackbar.make(binding.getRoot(), state.getErrorMessage(), Snackbar.LENGTH_LONG).show();
        }

        if (state.isSaved()) {
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    private void buildRoleChecklist(UserEditUiState state) {
        binding.roleChecklist.removeAllViews();
        if (state.getRoles().isEmpty()) {
            return;
        }
        for (RoleResponse role : state.getRoles()) {
            if (role.getId() == null) continue;
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(
                    role.getName() == null || role.getName().trim().isEmpty()
                            ? getString(R.string.role_unnamed)
                            : role.getName()
            );
            checkBox.setChecked(state.getCheckedRoleIds().contains(role.getId()));
            long roleId = role.getId();
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                    viewModel.toggleRole(roleId, isChecked)
            );
            binding.roleChecklist.addView(checkBox);
        }
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }

    public static Intent newIntent(Activity activity, long userId) {
        Intent intent = new Intent(activity, UserEditActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        return intent;
    }
}