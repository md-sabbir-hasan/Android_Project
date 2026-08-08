package com.nexaerp.mobile.feature.user;

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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.nexaerp.mobile.R;
import com.nexaerp.mobile.data.remote.api.UserApi;
import com.nexaerp.mobile.data.remote.client.RetrofitClient;
import com.nexaerp.mobile.data.repository.UserRepository;
import com.nexaerp.mobile.databinding.ActivityUserListBinding;

public class UserListActivity extends AppCompatActivity {

    private static final int LOAD_MORE_THRESHOLD = 4;

    private ActivityUserListBinding binding;
    private UserListViewModel viewModel;
    private UserAdapter adapter;
    private String lastShownTransientMessage;

    private final ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> viewModel.refresh()
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UserApi userApi = RetrofitClient.createService(getApplicationContext(), UserApi.class);
        UserRepository repository = new UserRepository(userApi);
        viewModel = new ViewModelProvider(
                this, new UserListViewModelFactory(repository)
        ).get(UserListViewModel.class);

        setupToolbar();
        setupSearch();
        setupStatusFilter();
        setupList();

        binding.swipeRefresh.setOnRefreshListener(viewModel::refresh);
        binding.retryButton.setOnClickListener(ignored -> viewModel.retry());

        viewModel.getState().observe(this, this::render);
        viewModel.loadFirstPage();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.inflateMenu(R.menu.menu_user_list);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_new_user) {
                editLauncher.launch(UserEditActivity.newIntent(this, 0));
                return true;
            }
            return false;
        });
    }

    private void setupSearch() {
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearch(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupStatusFilter() {
        binding.filterGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(binding.chipActive.getId())) {
                viewModel.setStatusFilter("ACTIVE");
            } else if (checkedIds.contains(binding.chipInactive.getId())) {
                viewModel.setStatusFilter("INACTIVE");
            } else if (checkedIds.contains(binding.chipLocked.getId())) {
                viewModel.setStatusFilter("LOCKED");
            } else if (checkedIds.contains(binding.chipPending.getId())) {
                viewModel.setStatusFilter("PENDING");
            } else {
                viewModel.setStatusFilter(null);
            }
        });
    }

    private void setupList() {
        adapter = new UserAdapter(user -> {
            if (user.getId() != null) {
                startActivity(UserDetailActivity.newIntent(this, user.getId()));
            }
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@androidx.annotation.NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0) return;
                LinearLayoutManager layoutManager =
                        (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                int total = layoutManager.getItemCount();
                if (lastVisible >= total - LOAD_MORE_THRESHOLD) {
                    viewModel.loadMore();
                }
            }
        });
    }

    private void render(UserListUiState state) {
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
        adapter.submitList(state.getItems());

        if (state.getTransientMessage() != null
                && !state.getTransientMessage().equals(lastShownTransientMessage)) {
            lastShownTransientMessage = state.getTransientMessage();
            Snackbar.make(binding.getRoot(), state.getTransientMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }

    public static Intent newIntent(Activity activity) {
        return new Intent(activity, UserListActivity.class);
    }
}