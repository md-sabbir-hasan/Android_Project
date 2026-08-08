package com.nexaerp.mobile.feature.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nexaerp.mobile.data.repository.UserRepository;

public class UserListViewModel extends ViewModel {
    private static final int PAGE_SIZE = 20;

    private final UserRepository repository;
    private final MutableLiveData<UserListUiState> state =
            new MutableLiveData<>(UserListUiState.initial());

    private int nextPage;
    private boolean requestInFlight;

    public UserListViewModel(UserRepository repository) {
        this.repository = repository;
    }

    public LiveData<UserListUiState> getState() {
        return state;
    }

    public void loadFirstPage() {
        UserListUiState current = state.getValue();
        if (requestInFlight || (current != null && !current.getItems().isEmpty())) return;
        fetchPage(false);
    }

    public void retry() {
        if (requestInFlight) return;
        UserListUiState current = state.getValue();
        if (current != null) state.setValue(current.withLoading());
        fetchPage(false);
    }

    public void refresh() {
        if (requestInFlight) return;
        UserListUiState current = state.getValue();
        if (current != null) state.setValue(current.withRefreshing());
        fetchPage(false);
    }

    public void loadMore() {
        UserListUiState current = state.getValue();
        if (requestInFlight || current == null || !current.hasMore()
                || current.isLoading() || current.isRefreshing()) {
            return;
        }
        state.setValue(current.withLoadingMore());
        fetchPage(true);
    }

    public void setSearch(String search) {
        UserListUiState current = state.getValue();
        if (current == null) return;
        state.setValue(current.withSearch(search));
        fetchPage(false);
    }

    public void setStatusFilter(String status) {
        UserListUiState current = state.getValue();
        if (current == null || safeEquals(current.getStatusFilter(), status)) return;
        state.setValue(current.withStatusFilter(status));
        fetchPage(false);
    }

    private void fetchPage(boolean append) {
        UserListUiState current = state.getValue();
        String search = current == null || current.getSearch() == null || current.getSearch().trim().isEmpty()
                ? null : current.getSearch().trim();
        String status = current == null ? null : current.getStatusFilter();
        int page = append ? nextPage : 0;

        requestInFlight = true;
        repository.loadUsers(page, PAGE_SIZE, search, status, result -> {
            requestInFlight = false;
            UserListUiState latest = state.getValue();
            if (latest == null) return;

            if (!result.isSuccess()) {
                if (append) {
                    nextPage = page;
                    state.setValue(latest.withTransientError(result.getErrorMessage()));
                } else if (latest.getItems().isEmpty()) {
                    state.setValue(latest.withFatalError(result.getErrorMessage()));
                } else {
                    state.setValue(latest.withTransientError(result.getErrorMessage()));
                }
                return;
            }

            nextPage = page + 1;
            if (append) {
                state.setValue(latest.withAppendedPage(result.getItems(), result.isLast()));
            } else {
                state.setValue(latest.withPage(result.getItems(), result.isLast()));
            }
        });
    }

    private static boolean safeEquals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    @Override
    protected void onCleared() {
        repository.cancel();
    }
}