package com.nexaerp.mobile.data.remote.model.dashboard;

import java.util.List;

public class DashboardSummaryResponse {
    private UserSummaryResponse users;
    private SecuritySummaryResponse security;
    private FinanceSummaryResponse finance;
    private BusinessSummaryResponse business;
    private SystemSummaryResponse system;
    private List<RecentActivityResponse> recentActivities;
    private BudgetDashboardResponse budget;
    private ExpenseDashboardResponse expense;

    public DashboardSummaryResponse() {}
    public UserSummaryResponse getUsers() { return users; }
    public void setUsers(UserSummaryResponse users) { this.users = users; }
    public SecuritySummaryResponse getSecurity() { return security; }
    public void setSecurity(SecuritySummaryResponse security) { this.security = security; }
    public FinanceSummaryResponse getFinance() { return finance; }
    public void setFinance(FinanceSummaryResponse finance) { this.finance = finance; }
    public BusinessSummaryResponse getBusiness() { return business; }
    public void setBusiness(BusinessSummaryResponse business) { this.business = business; }
    public SystemSummaryResponse getSystem() { return system; }
    public void setSystem(SystemSummaryResponse system) { this.system = system; }
    public List<RecentActivityResponse> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<RecentActivityResponse> recentActivities) { this.recentActivities = recentActivities; }
    public BudgetDashboardResponse getBudget() { return budget; }
    public void setBudget(BudgetDashboardResponse budget) { this.budget = budget; }
    public ExpenseDashboardResponse getExpense() { return expense; }
    public void setExpense(ExpenseDashboardResponse expense) { this.expense = expense; }
}
