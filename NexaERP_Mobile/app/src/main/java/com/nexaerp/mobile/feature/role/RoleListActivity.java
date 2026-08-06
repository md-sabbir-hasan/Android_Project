package com.nexaerp.mobile.feature.role;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nexaerp.mobile.R;
import com.nexaerp.mobile.data.remote.api.PermissionApi;
import com.nexaerp.mobile.data.remote.api.RoleApi;
import com.nexaerp.mobile.data.remote.client.RetrofitClient;
import com.nexaerp.mobile.data.repository.RoleRepository;
import com.nexaerp.mobile.databinding.ActivityRoleListBinding;

public class RoleListActivity extends AppCompatActivity {

    private ActivityRoleListBinding binding;
    private RoleListViewModel viewModel;
    private RoleAdapter adapter;

    private final ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> viewModel.refresh()
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoleListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        RoleApi roleApi = RetrofitClient.createService(getApplicationContext(), RoleApi.class);
        PermissionApi permissionApi = RetrofitClient.createService(
                getApplicationContext(), PermissionApi.class
        );
        RoleRepository repository = new RoleRepository(roleApi, permissionApi);
        viewModel = new ViewModelProvider(
                this, new RoleListViewModelFactory(repository)
        ).get(RoleListViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.inflateMenu(R.menu.menu_role_list);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_new_role) {
                editLauncher.launch(RoleEditActivity.newIntent(this, 0));
                return true;
            }
            return false;
        });

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setQuery(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        adapter = new RoleAdapter(role -> {
            if (role.getId() != null) {
                startActivity(RoleDetailActivity.newIntent(this, role.getId()));
            }
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(viewModel::refresh);
        binding.retryButton.setOnClickListener(ignored -> viewModel.load());

        viewModel.getState().observe(this, this::render);
        viewModel.load();
    }

    private void render(RoleListUiState state) {
        binding.swipeRefresh.setRefreshing(state.isRefreshing());

        boolean fatal = state.getErrorMessage() != null;
        binding.loadingState.setVisibility(state.isLoading() && !fatal ? View.VISIBLE : View.GONE);
        binding.errorState.setVisibility(fatal ? View.VISIBLE : View.GONE);
        if (fatal) {
            binding.errorMessage.setText(state.getErrorMessage());
            binding.swipeRefresh.setVisibility(View.GONE);
            binding.emptyState.setVisibility(View.GONE);
            return;
        }

        boolean empty = state.isEmpty();
        binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.swipeRefresh.setVisibility(state.isLoading() || empty ? View.GONE : View.VISIBLE);
        adapter.submitList(state.getFilteredRoles());
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }

    public static Intent newIntent(Activity activity) {
        return new Intent(activity, RoleListActivity.class);
    }
}