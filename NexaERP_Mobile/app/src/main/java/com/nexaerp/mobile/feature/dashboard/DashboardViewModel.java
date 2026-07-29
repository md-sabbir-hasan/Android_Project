package com.nexaerp.mobile.feature.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nexaerp.mobile.data.remote.model.dashboard.DashboardSummaryResponse;
import com.nexaerp.mobile.data.repository.DashboardRepository;

public class DashboardViewModel extends ViewModel {
    private final DashboardRepository repository;
    private final MutableLiveData<DashboardUiState> state =
            new MutableLiveData<>(DashboardUiState.initialLoading());
    private boolean requestInFlight;

    public DashboardViewModel(DashboardRepository repository) {
        this.repository = repository;
    }

    public LiveData<DashboardUiState> getState() {
        return state;
    }

    public void loadDashboard() {
        DashboardUiState current = state.getValue();
        if (requestInFlight || (current != null && current.getData() != null)) {
            return;
        }
        request(false);
    }

    public void refreshDashboard() {
        if (requestInFlight) {
            return;
        }
        request(true);
    }

    public void retry() {
        if (requestInFlight) {
            return;
        }
        request(false);
    }

    private void request(boolean refresh) {
        requestInFlight = true;
        DashboardUiState current = state.getValue();
        DashboardSummaryResponse retained = current == null ? null : current.getData();
        state.setValue(refresh && retained != null
                ? DashboardUiState.refreshing(retained)
                : DashboardUiState.initialLoading());

        repository.loadDashboard(result -> {
            requestInFlight = false;
            if (result.isSuccess()) {
                state.setValue(DashboardUiState.content(result.getData()));
            } else if (retained != null) {
                state.setValue(DashboardUiState.contentWithError(
                        retained,
                        result.getErrorMessage(),
                        result.isRetryable()
                ));
            } else {
                state.setValue(DashboardUiState.fatalError(
                        result.getErrorMessage(),
                        result.isRetryable()
                ));
            }
        });
    }

    @Override
    protected void onCleared() {
        repository.cancel();
    }
}
