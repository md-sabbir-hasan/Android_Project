package com.nexaerp.mobile.feature.dashboard;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.nexaerp.mobile.data.repository.DashboardRepository;
import com.nexaerp.mobile.data.repository.NotificationRepository;

public final class DashboardViewModelFactory implements ViewModelProvider.Factory {
    private final DashboardRepository dashboardRepository;
    private final NotificationRepository notificationRepository;

    public DashboardViewModelFactory(
            DashboardRepository dashboardRepository,
            NotificationRepository notificationRepository
    ) {
        this.dashboardRepository = dashboardRepository;
        this.notificationRepository = notificationRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DashboardViewModel.class)) {
            return (T) new DashboardViewModel(dashboardRepository, notificationRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
