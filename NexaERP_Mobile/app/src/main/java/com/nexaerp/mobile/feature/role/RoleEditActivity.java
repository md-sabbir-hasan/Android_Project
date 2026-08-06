package com.nexaerp.mobile.feature.role;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.nexaerp.mobile.R;
import com.nexaerp.mobile.data.remote.api.PermissionApi;
import com.nexaerp.mobile.data.remote.api.RoleApi;
import com.nexaerp.mobile.data.remote.client.RetrofitClient;
import com.nexaerp.mobile.data.remote.model.role.PermissionResponse;
import com.nexaerp.mobile.data.repository.RoleRepository;
import com.nexaerp.mobile.databinding.ActivityRoleEditBinding;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RoleEditActivity extends AppCompatActivity {

    private static final String EXTRA_ROLE_ID = "extra_role_id";

    private ActivityRoleEditBinding binding;
    private RoleEditViewModel viewModel;
    private boolean permissionViewsBuilt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoleEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        long roleId = getIntent().getLongExtra(EXTRA_ROLE_ID, 0);

        RoleApi roleApi = RetrofitClient.createService(getApplicationContext(), RoleApi.class);
        PermissionApi permissionApi = RetrofitClient.createService(
                getApplicationContext(), PermissionApi.class
        );
        RoleRepository repository = new RoleRepository(roleApi, permissionApi);
        viewModel = new ViewModelProvider(
                this, new RoleEditViewModelFactory(repository, roleId)
        ).get(RoleEditViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.setTitle(
                viewModel.isEditMode() ? R.string.role_edit_title : R.string.role_create_title
        );

        binding.nameInput.setOnFocusChangeListener((v, hasFocus) -> syncFieldsToViewModel());
        binding.descriptionInput.setOnFocusChangeListener((v, hasFocus) -> syncFieldsToViewModel());
        binding.saveButton.setOnClickListener(ignored -> {
            syncFieldsToViewModel();
            viewModel.save();
        });
        binding.retryButton.setOnClickListener(ignored -> viewModel.load());

        viewModel.getState().observe(this, this::render);
        viewModel.load();
    }

    private void syncFieldsToViewModel() {
        viewModel.setFields(
                binding.nameInput.getText() == null ? "" : binding.nameInput.getText().toString(),
                binding.descriptionInput.getText() == null
                        ? ""
                        : binding.descriptionInput.getText().toString()
        );
    }

    private void render(RoleEditUiState state) {
        boolean fatal = state.getErrorMessage() != null && state.getPermissions().isEmpty();
        binding.loadingState.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
        binding.errorState.setVisibility(fatal ? View.VISIBLE : View.GONE);
        binding.formContent.setVisibility(
                !state.isLoading() && !fatal ? View.VISIBLE : View.GONE
        );
        if (fatal) {
            binding.errorMessage.setText(state.getErrorMessage());
            return;
        }
        if (state.isLoading()) return;

        if (!permissionViewsBuilt) {
            binding.nameInput.setText(state.getName());
            binding.descriptionInput.setText(state.getDescription());
            buildPermissionChecklist(state);
            permissionViewsBuilt = true;
        }

        binding.saveButton.setEnabled(!state.isSaving());
        binding.saveButton.setText(
                state.isSaving() ? R.string.role_saving : R.string.role_save
        );

        if (state.getErrorMessage() != null && !state.getPermissions().isEmpty()) {
            Snackbar.make(binding.getRoot(), state.getErrorMessage(), Snackbar.LENGTH_LONG).show();
        }

        if (state.isSaved()) {
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    private void buildPermissionChecklist(RoleEditUiState state) {
        binding.permissionGroups.removeAllViews();

        Map<String, List<PermissionResponse>> byModule = new LinkedHashMap<>();
        List<PermissionResponse> sorted = new ArrayList<>(state.getPermissions());
        sorted.sort(Comparator.comparing(
                (PermissionResponse p) -> p.getModule() == null ? "" : p.getModule()
        ));
        for (PermissionResponse permission : sorted) {
            String module = permission.getModule() == null || permission.getModule().trim().isEmpty()
                    ? getString(R.string.role_module_general)
                    : permission.getModule();
            byModule.computeIfAbsent(module, key -> new ArrayList<>()).add(permission);
        }

        for (Map.Entry<String, List<PermissionResponse>> entry : byModule.entrySet()) {
            TextView header = new TextView(this);
            header.setText(entry.getKey());
            header.setTextAppearance(
                    com.google.android.material.R.style.TextAppearance_Material3_TitleSmall
            );
            header.setPadding(0, dp(14), 0, dp(4));
            binding.permissionGroups.addView(header);

            for (PermissionResponse permission : entry.getValue()) {
                if (permission.getId() == null) continue;
                CheckBox checkBox = new CheckBox(this);
                checkBox.setText(
                        permission.getName() == null ? permission.getCode() : permission.getName()
                );
                checkBox.setChecked(state.getCheckedPermissionIds().contains(permission.getId()));
                long permissionId = permission.getId();
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                        viewModel.togglePermission(permissionId, isChecked)
                );
                binding.permissionGroups.addView(checkBox);
            }
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }

    public static Intent newIntent(Activity activity, long roleId) {
        Intent intent = new Intent(activity, RoleEditActivity.class);
        intent.putExtra(EXTRA_ROLE_ID, roleId);
        return intent;
    }
}