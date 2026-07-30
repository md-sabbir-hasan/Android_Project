package com.nexaerp.mobile.feature.dashboard;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.nexaerp.mobile.R;
import com.nexaerp.mobile.core.formatting.MoneyFormatter;
import com.nexaerp.mobile.core.permission.PermissionCodes;
import com.nexaerp.mobile.core.permission.PermissionEvaluator;
import com.nexaerp.mobile.data.remote.api.DashboardApi;
import com.nexaerp.mobile.data.remote.api.NotificationApi;
import com.nexaerp.mobile.data.remote.client.RetrofitClient;
import com.nexaerp.mobile.data.remote.model.dashboard.BusinessSummaryResponse;
import com.nexaerp.mobile.data.remote.model.dashboard.DashboardSummaryResponse;
import com.nexaerp.mobile.data.remote.model.dashboard.ExpenseDashboardResponse;
import com.nexaerp.mobile.data.remote.model.dashboard.RecentActivityResponse;
import com.nexaerp.mobile.data.repository.DashboardRepository;
import com.nexaerp.mobile.data.repository.NotificationRepository;
import com.nexaerp.mobile.databinding.FragmentDashboardBinding;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DashboardFragment extends Fragment {
    public interface LogoutCallback {
        void onLogoutRequested();
    }

    private static final String ARG_NAME = "name";
    private static final String ARG_ROLES = "roles";
    private static final String ARG_PERMISSIONS = "permissions";

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private String userName;
    private Set<String> roles = Collections.emptySet();
    private Set<String> permissions = Collections.emptySet();
    private String lastShownError;
    private LogoutCallback logoutCallback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!(context instanceof LogoutCallback)) {
            throw new IllegalStateException(
                    "Dashboard host must implement DashboardFragment.LogoutCallback"
            );
        }
        logoutCallback = (LogoutCallback) context;
    }

    public static DashboardFragment newInstance(
            String name,
            Set<String> roles,
            Set<String> permissions
    ) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_NAME, name);
        arguments.putStringArray(ARG_ROLES, toArray(roles));
        arguments.putStringArray(ARG_PERMISSIONS, toArray(permissions));
        fragment.setArguments(arguments);
        return fragment;
    }

    private static String[] toArray(Set<String> values) {
        return values == null ? new String[0] : values.toArray(new String[0]);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            userName = arguments.getString(ARG_NAME);
            roles = asSet(arguments.getStringArray(ARG_ROLES));
            permissions = asSet(arguments.getStringArray(ARG_PERMISSIONS));
        }
    }

    private Set<String> asSet(String[] values) {
        return values == null
                ? Collections.emptySet()
                : new LinkedHashSet<>(Arrays.asList(values));
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        DashboardApi api = RetrofitClient.createService(
                requireContext().getApplicationContext(),
                DashboardApi.class
        );
        DashboardRepository repository = new DashboardRepository(api);
        NotificationApi notificationApi = RetrofitClient.createService(
                requireContext().getApplicationContext(),
                NotificationApi.class
        );
        NotificationRepository notificationRepository =
                new NotificationRepository(notificationApi);
        viewModel = new ViewModelProvider(
                this,
                new DashboardViewModelFactory(repository, notificationRepository)
        ).get(DashboardViewModel.class);

        bindIdentity();
        bindQuickActions();
        binding.notificationButton.setOnClickListener(ignored -> Snackbar.make(
                binding.getRoot(),
                R.string.dashboard_notification_center_coming_next,
                Snackbar.LENGTH_SHORT
        ).show());
        binding.logoutButton.setOnClickListener(ignored -> requestLogout());
        binding.swipeRefresh.setOnRefreshListener(viewModel::refreshDashboard);
        binding.retryButton.setOnClickListener(ignored -> viewModel.retry());
        viewModel.getState().observe(getViewLifecycleOwner(), this::render);
        viewModel.loadDashboard();
    }

    private void requestLogout() {
        FragmentDashboardBinding currentBinding = binding;
        LogoutCallback callback = logoutCallback;
        if (currentBinding == null || callback == null
                || !currentBinding.logoutButton.isEnabled()) {
            return;
        }
        currentBinding.logoutButton.setEnabled(false);
        callback.onLogoutRequested();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refreshUnreadCountIfStale();
        }
    }

    private void bindIdentity() {
        String safeName = isBlank(userName) ? getString(R.string.dashboard_user) : userName.trim();
        binding.greeting.setText(getString(R.string.dashboard_greeting, safeName));
        binding.roleLabel.setText(readableRoles());
    }

    private String readableRoles() {
        if (roles.isEmpty()) {
            return getString(R.string.dashboard_role_unavailable);
        }
        StringBuilder result = new StringBuilder();
        for (String role : roles) {
            if (result.length() > 0) {
                result.append(", ");
            }
            String readable = role == null ? "" : role.replace('_', ' ').toLowerCase(Locale.getDefault());
            String[] words = readable.split(" ");
            for (int index = 0; index < words.length; index++) {
                if (index > 0) result.append(' ');
                if (!words[index].isEmpty()) {
                    result.append(Character.toUpperCase(words[index].charAt(0)))
                            .append(words[index].substring(1));
                }
            }
        }
        return result.toString();
    }

    private void bindQuickActions() {
        PermissionEvaluator evaluator = new PermissionEvaluator(permissions);
        for (QuickAction action : QuickActionProvider.permitted(evaluator)) {
            Chip chip = new Chip(requireContext());
            chip.setText(action.getLabel());
            chip.setClickable(true);
            chip.setCheckable(false);
            chip.setOnClickListener(view -> Snackbar.make(
                    binding.getRoot(),
                    R.string.dashboard_feature_coming_next,
                    Snackbar.LENGTH_SHORT
            ).show());
            binding.quickActions.addView(chip);
        }
        binding.quickActionsSection.setVisibility(
                binding.quickActions.getChildCount() == 0 ? View.GONE : View.VISIBLE
        );
    }

    private void render(DashboardUiState state) {
        renderNotificationBadge(state.getUnreadCount());
        binding.swipeRefresh.setRefreshing(state.isRefreshing());
        binding.loadingState.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);

        boolean fatal = state.getData() == null && state.getErrorMessage() != null && !state.isLoading();
        binding.errorState.setVisibility(fatal ? View.VISIBLE : View.GONE);
        binding.swipeRefresh.setVisibility(fatal || state.isLoading() ? View.GONE : View.VISIBLE);
        if (fatal) {
            binding.errorMessage.setText(state.getErrorMessage());
            binding.retryButton.setVisibility(state.isRetryable() ? View.VISIBLE : View.GONE);
            return;
        }

        if (state.getData() != null) {
            renderContent(state.getData(), state.isEmptyOrAccessLimited());
        }
        if (state.getData() != null
                && state.getErrorMessage() != null
                && !state.getErrorMessage().equals(lastShownError)) {
            lastShownError = state.getErrorMessage();
            Snackbar.make(binding.getRoot(), state.getErrorMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private void renderContent(DashboardSummaryResponse data, boolean empty) {
        binding.lastUpdated.setText(getString(
                R.string.dashboard_last_updated,
                DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date())
        ));
        binding.emptyMessage.setVisibility(empty ? View.VISIBLE : View.GONE);
        renderFinancialOverview(data);
        renderAttention(data);
        renderRecentActivity(data);
    }

    private void renderNotificationBadge(Long count) {
        boolean visible = NotificationBadgeFormatter.isVisible(count);
        binding.notificationBadge.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            binding.notificationBadge.setText(NotificationBadgeFormatter.text(count));
        }
        if (count == null || count <= 0L) {
            binding.notificationButton.setContentDescription(
                    getString(R.string.dashboard_no_unread_notifications)
            );
        } else if (count > 99L) {
            binding.notificationButton.setContentDescription(
                    getString(R.string.dashboard_over_99_unread_notifications)
            );
        } else {
            binding.notificationButton.setContentDescription(
                    getString(R.string.dashboard_unread_notifications, count)
            );
        }
    }

    private void renderFinancialOverview(DashboardSummaryResponse data) {
        binding.metricsContainer.removeAllViews();
        BusinessSummaryResponse business = data.getBusiness();
        ExpenseDashboardResponse expense = data.getExpense();
        String currency = business == null ? null : business.getCurrencyCode();

        if (business != null) {
            if (Boolean.FALSE.equals(business.getCashConfigured())) {
                addMetric(getString(R.string.dashboard_cash_position),
                        getString(R.string.dashboard_cash_not_configured));
            } else if (business.getCashPosition() != null) {
                addMetric(getString(R.string.dashboard_cash_position),
                        MoneyFormatter.format(business.getCashPosition(), currency));
            }
            addMoneyMetric(R.string.dashboard_receivable, business.getAccountsReceivable(), currency);
            addMoneyMetric(R.string.dashboard_payable, business.getAccountsPayable(), currency);
        }
        if (expense != null) {
            addMoneyMetric(
                    R.string.dashboard_month_expense,
                    expense.getPostedThisMonthTotal(),
                    currency
            );
        }
        binding.financialSection.setVisibility(
                binding.metricsContainer.getChildCount() == 0 ? View.GONE : View.VISIBLE
        );
    }

    private void addMoneyMetric(int label, BigDecimal amount, String currency) {
        if (amount != null) {
            addMetric(getString(label), MoneyFormatter.format(amount, currency));
        }
    }

    private void addMetric(String label, String value) {
        MaterialCardView card = new MaterialCardView(requireContext());
        card.setRadius(dp(16));
        card.setCardElevation(0);
        card.setStrokeWidth(dp(1));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.topMargin = dp(8);
        card.setLayoutParams(cardParams);

        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(14), dp(16), dp(14));
        TextView labelView = new TextView(requireContext());
        labelView.setText(label);
        labelView.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_LabelLarge);
        TextView valueView = new TextView(requireContext());
        valueView.setText(value);
        valueView.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleLarge);
        valueView.setPadding(0, dp(4), 0, 0);
        content.addView(labelView);
        content.addView(valueView);
        card.addView(content);
        binding.metricsContainer.addView(card);
    }

    private void renderAttention(DashboardSummaryResponse data) {
        binding.attentionItems.removeAllViews();
        BusinessSummaryResponse business = data.getBusiness();
        ExpenseDashboardResponse expense = data.getExpense();
        if (business != null) {
            if (positive(business.getOverdueInvoiceCount())) {
                addAttention(getResources().getQuantityString(
                        R.plurals.dashboard_overdue_invoices,
                        safeInt(business.getOverdueInvoiceCount()),
                        business.getOverdueInvoiceCount()
                ));
            }
            if (positive(business.getOverdueBillCount())) {
                addAttention(getResources().getQuantityString(
                        R.plurals.dashboard_overdue_bills,
                        safeInt(business.getOverdueBillCount()),
                        business.getOverdueBillCount()
                ));
            }
        }
        if (expense != null && expense.getDraftCount() > 0) {
            addAttention(getResources().getQuantityString(
                    R.plurals.dashboard_draft_expenses,
                    safeInt(expense.getDraftCount()),
                    expense.getDraftCount()
            ));
        }
        if (expense != null && expense.getRecurringDueSoonCount() > 0) {
            addAttention(getResources().getQuantityString(
                    R.plurals.dashboard_recurring_due,
                    safeInt(expense.getRecurringDueSoonCount()),
                    expense.getRecurringDueSoonCount()
            ));
        }
        binding.attentionSection.setVisibility(
                binding.attentionItems.getChildCount() == 0 ? View.GONE : View.VISIBLE
        );
    }

    private void addAttention(String message) {
        TextView item = new TextView(requireContext());
        item.setText(getString(R.string.dashboard_attention_item, message));
        item.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
        item.setPadding(0, dp(5), 0, dp(5));
        binding.attentionItems.addView(item);
    }

    private void renderRecentActivity(DashboardSummaryResponse data) {
        PermissionEvaluator evaluator = new PermissionEvaluator(permissions);
        List<RecentActivityResponse> activities = data.getRecentActivities();
        boolean permitted = evaluator.has(PermissionCodes.VIEW_AUDIT_LOGS);
        boolean returnedByBackend = activities != null;
        binding.recentActivitySection.setVisibility(
                permitted && returnedByBackend ? View.VISIBLE : View.GONE
        );
        binding.recentActivityItems.removeAllViews();
        if (!permitted || !returnedByBackend) {
            binding.recentActivityEmpty.setVisibility(View.GONE);
            return;
        }

        List<RecentActivityResponse> visible =
                RecentActivityPresenter.visibleItems(evaluator, activities);
        binding.recentActivityEmpty.setVisibility(
                visible.isEmpty() ? View.VISIBLE : View.GONE
        );
        for (RecentActivityResponse activity : visible) {
            addRecentActivity(activity);
        }
    }

    private void addRecentActivity(RecentActivityResponse activity) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.TOP);
        row.setPadding(0, dp(12), 0, dp(12));

        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(activityIcon(activity));
        icon.setContentDescription(null);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        iconParams.setMarginEnd(dp(14));
        icon.setLayoutParams(iconParams);

        LinearLayout text = new LinearLayout(requireContext());
        text.setOrientation(LinearLayout.VERTICAL);
        text.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        ));

        TextView title = new TextView(requireContext());
        title.setText(RecentActivityPresenter.safeTitle(activity));
        title.setTextAppearance(
                com.google.android.material.R.style.TextAppearance_Material3_TitleSmall
        );
        text.addView(title);

        String details = activityDetails(activity);
        if (!details.isEmpty()) {
            TextView detailView = new TextView(requireContext());
            detailView.setText(details);
            detailView.setTextAppearance(
                    com.google.android.material.R.style.TextAppearance_Material3_BodySmall
            );
            text.addView(detailView);
        }

        TextView timestamp = new TextView(requireContext());
        timestamp.setText(formatActivityTime(activity));
        timestamp.setTextAppearance(
                com.google.android.material.R.style.TextAppearance_Material3_LabelMedium
        );
        timestamp.setPadding(0, dp(3), 0, 0);
        text.addView(timestamp);

        row.addView(icon);
        row.addView(text);
        binding.recentActivityItems.addView(row);
    }

    private String activityDetails(RecentActivityResponse activity) {
        StringBuilder details = new StringBuilder();
        if (activity.getEntityId() != null) {
            details.append(getString(
                    R.string.dashboard_activity_reference,
                    activity.getEntityId()
            ));
        }
        if (!isBlank(activity.getUserName())) {
            if (details.length() > 0) details.append(" \u2022 ");
            details.append(getString(
                    R.string.dashboard_activity_by,
                    activity.getUserName().trim()
            ));
        }
        return details.toString();
    }

    private String formatActivityTime(RecentActivityResponse activity) {
        if (activity == null || activity.getCreatedAt() == null) {
            return getString(R.string.dashboard_activity_time_unavailable);
        }
        // Backend LocalDateTime has no offset. Formatting it as server-local wall time avoids
        // claiming a timezone conversion that the wire contract cannot support.
        return activity.getCreatedAt().format(DateTimeFormatter.ofLocalizedDateTime(
                FormatStyle.MEDIUM,
                FormatStyle.SHORT
        ).withLocale(Locale.getDefault()));
    }

    private int activityIcon(RecentActivityResponse activity) {
        String action = activity == null || activity.getAction() == null
                ? ""
                : activity.getAction().toUpperCase(Locale.ROOT);
        if (action.contains("CREATE") || action.contains("LOGIN")) {
            return android.R.drawable.ic_input_add;
        }
        if (action.contains("DELETE") || action.contains("CANCEL")) {
            return android.R.drawable.ic_menu_delete;
        }
        if (action.contains("UPDATE") || action.contains("EDIT")) {
            return android.R.drawable.ic_menu_edit;
        }
        return android.R.drawable.ic_menu_info_details;
    }

    private boolean positive(Long value) {
        return value != null && value > 0;
    }

    private int safeInt(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        logoutCallback = null;
        super.onDetach();
    }
}
