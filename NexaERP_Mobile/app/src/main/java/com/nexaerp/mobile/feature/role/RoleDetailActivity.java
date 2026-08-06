package com.nexaerp.mobile.feature.role;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.nexaerp.mobile.R;
import com.nexaerp.mobile.data.remote.api.PermissionApi;
import com.nexaerp.mobile.data.remote.api.RoleApi;
import com.nexaerp.mobile.data.remote.client.RetrofitClient;
import com.nexaerp.mobile.data.remote.model.role.RoleResponse;
import com.nexaerp.mobile.data.repository.RoleRepository;
import com.nexaerp.mobile.databinding.ActivityRoleDetailBinding;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RoleDetailActivity extends AppCompatActivity {

    private static final String EXTRA_ROLE_ID = "extra_role_id";

    private ActivityRoleDetailBinding binding;
    private RoleDetailViewModel viewModel;
    private long roleId;

    private final ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> viewModel.load()
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoleDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        roleId = getIntent().getLongExtra(EXTRA_ROLE_ID, 0);

        RoleApi roleApi = RetrofitClient.createService(getApplicationContext(), RoleApi.class);
        PermissionApi permissionApi = RetrofitClient.createService(
                getApplicationContext(), PermissionApi.class
        );
        RoleRepository repository = new RoleRepository(roleApi, permissionApi);
        viewModel = new ViewModelProvider(
                this, new RoleDetailViewModelFactory(repository, roleId)
        ).get(RoleDetailViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.inflateMenu(R.menu.menu_role_detail);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_role) {
                editLauncher.launch(RoleEditActivity.newIntent(this, roleId));
                return true;
            }
            return false;
        });

        binding.retryButton.setOnClickListener(ignored -> viewModel.load());

        viewModel.getState().observe(this, this::render);
        viewModel.load();
    }

    private void render(RoleDetailUiState state) {
        binding.loadingState.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
        boolean fatal = state.getErrorMessage() != null;
        binding.errorState.setVisibility(fatal ? View.VISIBLE : View.GONE);
        binding.content.setVisibility(!state.isLoading() && !fatal ? View.VISIBLE : View.GONE);

        if (fatal) {
            binding.errorMessage.setText(state.getErrorMessage());
            return;
        }
        if (state.getRole() == null) return;

        RoleResponse role = state.getRole();
        binding.roleName.setText(
                role.getName() == null || role.getName().trim().isEmpty()
                        ? getString(R.string.role_unnamed)
                        : role.getName()
        );

        String description = role.getDescription();
        binding.roleDescription.setVisibility(
                description == null || description.trim().isEmpty() ? View.GONE : View.VISIBLE
        );
        binding.roleDescription.setText(description);

        int userCount = role.getUserCount() == null ? 0 : role.getUserCount();
        binding.roleUserCount.setText(
                getResources().getQuantityString(
                        R.plurals.role_user_count, userCount, userCount
                )
        );

        renderPermissionGroups(role);
    }

    private void renderPermissionGroups(RoleResponse role) {
        binding.permissionGroups.removeAllViews();

        if (role.getPermissions() == null || role.getPermissions().isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(R.string.role_no_permissions);
            empty.setPadding(0, dp(4), 0, dp(4));
            binding.permissionGroups.addView(empty);
            return;
        }

        Map<String, List<RoleResponse.PermissionSummary>> byModule = new LinkedHashMap<>();
        List<RoleResponse.PermissionSummary> sorted =
                new ArrayList<>(role.getPermissions());
        sorted.sort(Comparator.comparing(
                p -> p.getModule() == null ? "" : p.getModule()
        ));
        for (RoleResponse.PermissionSummary permission : sorted) {
            String module = permission.getModule() == null || permission.getModule().trim().isEmpty()
                    ? getString(R.string.role_module_general)
                    : permission.getModule();
            byModule.computeIfAbsent(module, key -> new ArrayList<>()).add(permission);
        }

        for (Map.Entry<String, List<RoleResponse.PermissionSummary>> entry : byModule.entrySet()) {
            TextView header = new TextView(this);
            header.setText(entry.getKey());
            header.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleSmall);
            header.setPadding(0, dp(14), 0, dp(4));
            binding.permissionGroups.addView(header);

            for (RoleResponse.PermissionSummary permission : entry.getValue()) {
                TextView row = new TextView(this);
                row.setText(permission.getName() == null ? permission.getCode() : permission.getName());
                row.setPadding(dp(4), dp(4), 0, dp(4));
                binding.permissionGroups.addView(row);
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
        Intent intent = new Intent(activity, RoleDetailActivity.class);
        intent.putExtra(EXTRA_ROLE_ID, roleId);
        return intent;
    }
}