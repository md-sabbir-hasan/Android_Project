package com.nexaerp.mobile.feature.notification;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.nexaerp.mobile.R;
import com.nexaerp.mobile.data.remote.api.NotificationApi;
import com.nexaerp.mobile.data.remote.client.RetrofitClient;
import com.nexaerp.mobile.data.remote.model.notification.NotificationItemResponse;
import com.nexaerp.mobile.data.repository.NotificationRepository;
import com.nexaerp.mobile.databinding.ActivityNotificationBinding;

public class NotificationActivity extends AppCompatActivity {

    private static final int NEAR_BOTTOM_THRESHOLD = 3;

    private ActivityNotificationBinding binding;
    private NotificationViewModel viewModel;
    private NotificationAdapter adapter;
    private String lastShownActionError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(view -> finish());
        binding.toolbar.inflateMenu(R.menu.menu_notifications);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_mark_all_read) {
                viewModel.markAllAsRead();
                return true;
            }
            return false;
        });

        NotificationApi api = RetrofitClient.createService(
                getApplicationContext(),
                NotificationApi.class
        );
        NotificationRepository repository = new NotificationRepository(api);
        viewModel = new ViewModelProvider(
                this,
                new NotificationViewModelFactory(repository)
        ).get(NotificationViewModel.class);

        adapter = new NotificationAdapter(this::onItemClicked);
        binding.notificationList.setLayoutManager(new LinearLayoutManager(this));
        binding.notificationList.setAdapter(adapter);
        binding.notificationList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0) {
                    return;
                }
                LinearLayoutManager layoutManager =
                        (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) {
                    return;
                }
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                int totalItemCount = layoutManager.getItemCount();
                if (lastVisible >= 0 && lastVisible >= totalItemCount - 1 - NEAR_BOTTOM_THRESHOLD) {
                    viewModel.loadNextPage();
                }
            }
        });

        binding.swipeRefresh.setOnRefreshListener(viewModel::refresh);
        binding.retryButton.setOnClickListener(view -> viewModel.retry());
        binding.unreadOnlyChip.setOnCheckedChangeListener(
                (chip, checked) -> viewModel.setUnreadOnly(checked)
        );

        viewModel.getState().observe(this, this::render);
        viewModel.loadFirstPage();
    }

    private void onItemClicked(NotificationItemResponse item) {
        viewModel.toggleRead(item);
    }

    private void render(NotificationUiState state) {
        binding.swipeRefresh.setRefreshing(state.isRefreshing());
        binding.loadMoreIndicator.setVisibility(state.isLoadingMore() ? View.VISIBLE : View.GONE);

        boolean fatal = state.isFatalError();
        binding.loadingState.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
        binding.errorState.setVisibility(fatal ? View.VISIBLE : View.GONE);
        binding.swipeRefresh.setVisibility(fatal || state.isLoading() ? View.GONE : View.VISIBLE);

        if (fatal) {
            binding.errorMessage.setText(state.getErrorMessage());
            binding.retryButton.setVisibility(state.isRetryable() ? View.VISIBLE : View.GONE);
            return;
        }

        adapter.submitList(state.getItems());
        binding.emptyMessage.setVisibility(state.isEmptyResult() ? View.VISIBLE : View.GONE);

        if (state.getActionError() != null
                && !state.getActionError().equals(lastShownActionError)) {
            lastShownActionError = state.getActionError();
            Snackbar.make(binding.getRoot(), state.getActionError(), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}