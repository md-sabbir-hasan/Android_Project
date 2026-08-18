package com.nexaerp.mobile.feature.expense;

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

import com.nexaerp.mobile.data.remote.api.AccountApi;
import com.nexaerp.mobile.data.remote.api.CostCenterApi;
import com.nexaerp.mobile.data.remote.api.ExpenseApi;
import com.nexaerp.mobile.data.remote.api.PartyApi;
import com.nexaerp.mobile.data.remote.client.RetrofitClient;
import com.nexaerp.mobile.data.repository.ExpenseRepository;
import com.nexaerp.mobile.databinding.ActivityExpenseListBinding;

public class ExpenseListActivity extends AppCompatActivity {

    private ActivityExpenseListBinding binding;
    private ExpenseListViewModel viewModel;
    private ExpenseAdapter adapter;

    private final ActivityResultLauncher<Intent> createLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> viewModel.refresh()
    );

    private final ActivityResultLauncher<Intent> detailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> viewModel.refresh()
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExpenseListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ExpenseRepository repository = buildRepository();
        viewModel = new ViewModelProvider(
                this, new ExpenseListViewModelFactory(repository)
        ).get(ExpenseListViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.fabAdd.setOnClickListener(v -> createLauncher.launch(
                ExpenseCreateActivity.newIntent(this)
        ));

        setupSearch();
        setupStatusFilter();
        setupList();

        binding.swipeRefresh.setOnRefreshListener(viewModel::refresh);
        binding.retryButton.setOnClickListener(ignored -> viewModel.load());

        viewModel.getState().observe(this, this::render);
        viewModel.load();
    }

    private ExpenseRepository buildRepository() {
        ExpenseApi expenseApi = RetrofitClient.createService(getApplicationContext(), ExpenseApi.class);
        AccountApi accountApi = RetrofitClient.createService(getApplicationContext(), AccountApi.class);
        CostCenterApi costCenterApi = RetrofitClient.createService(getApplicationContext(), CostCenterApi.class);
        PartyApi partyApi = RetrofitClient.createService(getApplicationContext(), PartyApi.class);
        return new ExpenseRepository(expenseApi, accountApi, costCenterApi, partyApi);
    }

    private void setupSearch() {
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
    }

    private void setupStatusFilter() {
        binding.filterGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(binding.chipDraft.getId())) {
                viewModel.setStatusFilter("DRAFT");
            } else if (checkedIds.contains(binding.chipPosted.getId())) {
                viewModel.setStatusFilter("POSTED");
            } else if (checkedIds.contains(binding.chipCancelled.getId())) {
                viewModel.setStatusFilter("CANCELLED");
            } else {
                viewModel.setStatusFilter(null);
            }
        });
    }

    private void setupList() {
        adapter = new ExpenseAdapter(expense -> {
            if (expense.getId() != null) {
                detailLauncher.launch(ExpenseDetailActivity.newIntent(this, expense.getId()));
            }
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private void render(ExpenseListUiState state) {
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
        adapter.submitList(state.getFilteredExpenses());
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }

    public static Intent newIntent(Activity activity) {
        return new Intent(activity, ExpenseListActivity.class);
    }
}