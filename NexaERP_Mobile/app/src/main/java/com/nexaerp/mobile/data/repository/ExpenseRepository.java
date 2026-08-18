package com.nexaerp.mobile.data.repository;

import androidx.annotation.NonNull;

import com.nexaerp.mobile.data.remote.api.AccountApi;
import com.nexaerp.mobile.data.remote.api.CostCenterApi;
import com.nexaerp.mobile.data.remote.api.ExpenseApi;
import com.nexaerp.mobile.data.remote.api.PartyApi;
import com.nexaerp.mobile.data.remote.model.ApiResponse;
import com.nexaerp.mobile.data.remote.model.account.AccountResponse;
import com.nexaerp.mobile.data.remote.model.costcenter.CostCenterResponse;
import com.nexaerp.mobile.data.remote.model.expense.ExpenseCancelRequest;
import com.nexaerp.mobile.data.remote.model.expense.ExpenseRequest;
import com.nexaerp.mobile.data.remote.model.expense.ExpenseResponse;
import com.nexaerp.mobile.data.remote.model.party.PartyResponse;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class ExpenseRepository {
    public interface ListResultCallback {
        void onResult(ListResult result);
    }

    public interface ItemResultCallback {
        void onResult(ItemResult result);
    }

    public interface AccountListCallback {
        void onResult(AccountListResult result);
    }

    public interface CostCenterListCallback {
        void onResult(CostCenterListResult result);
    }

    public interface PartyListCallback {
        void onResult(PartyListResult result);
    }

    public static final class ListResult {
        private final List<ExpenseResponse> items;
        private final String errorMessage;

        private ListResult(List<ExpenseResponse> items, String errorMessage) {
            this.items = items;
            this.errorMessage = errorMessage;
        }

        public static ListResult success(List<ExpenseResponse> items) {
            return new ListResult(items == null ? Collections.emptyList() : items, null);
        }

        public static ListResult error(String errorMessage) {
            return new ListResult(null, errorMessage);
        }

        public boolean isSuccess() { return items != null; }
        public List<ExpenseResponse> getItems() { return items; }
        public String getErrorMessage() { return errorMessage; }
    }

    public static final class ItemResult {
        private final ExpenseResponse item;
        private final String errorMessage;

        private ItemResult(ExpenseResponse item, String errorMessage) {
            this.item = item;
            this.errorMessage = errorMessage;
        }

        public static ItemResult success(ExpenseResponse item) {
            return new ItemResult(item, null);
        }

        public static ItemResult error(String errorMessage) {
            return new ItemResult(null, errorMessage);
        }

        public boolean isSuccess() { return item != null; }
        public ExpenseResponse getItem() { return item; }
        public String getErrorMessage() { return errorMessage; }
    }

    public static final class AccountListResult {
        private final List<AccountResponse> items;
        private final String errorMessage;

        private AccountListResult(List<AccountResponse> items, String errorMessage) {
            this.items = items;
            this.errorMessage = errorMessage;
        }

        public static AccountListResult success(List<AccountResponse> items) {
            return new AccountListResult(items == null ? Collections.emptyList() : items, null);
        }

        public static AccountListResult error(String errorMessage) {
            return new AccountListResult(null, errorMessage);
        }

        public boolean isSuccess() { return items != null; }
        public List<AccountResponse> getItems() { return items; }
        public String getErrorMessage() { return errorMessage; }
    }

    public static final class CostCenterListResult {
        private final List<CostCenterResponse> items;
        private final String errorMessage;

        private CostCenterListResult(List<CostCenterResponse> items, String errorMessage) {
            this.items = items;
            this.errorMessage = errorMessage;
        }

        public static CostCenterListResult success(List<CostCenterResponse> items) {
            return new CostCenterListResult(items == null ? Collections.emptyList() : items, null);
        }

        public static CostCenterListResult error(String errorMessage) {
            return new CostCenterListResult(null, errorMessage);
        }

        public boolean isSuccess() { return items != null; }
        public List<CostCenterResponse> getItems() { return items; }
        public String getErrorMessage() { return errorMessage; }
    }

    public static final class PartyListResult {
        private final List<PartyResponse> items;
        private final String errorMessage;

        private PartyListResult(List<PartyResponse> items, String errorMessage) {
            this.items = items;
            this.errorMessage = errorMessage;
        }

        public static PartyListResult success(List<PartyResponse> items) {
            return new PartyListResult(items == null ? Collections.emptyList() : items, null);
        }

        public static PartyListResult error(String errorMessage) {
            return new PartyListResult(null, errorMessage);
        }

        public boolean isSuccess() { return items != null; }
        public List<PartyResponse> getItems() { return items; }
        public String getErrorMessage() { return errorMessage; }
    }

    private final ExpenseApi expenseApi;
    private final AccountApi accountApi;
    private final CostCenterApi costCenterApi;
    private final PartyApi partyApi;

    public ExpenseRepository(
            ExpenseApi expenseApi, AccountApi accountApi, CostCenterApi costCenterApi, PartyApi partyApi
    ) {
        this.expenseApi = expenseApi;
        this.accountApi = accountApi;
        this.costCenterApi = costCenterApi;
        this.partyApi = partyApi;
    }

    public void loadExpenses(ListResultCallback callback) {
        expenseApi.getAll().enqueue(new Callback<ApiResponse<List<ExpenseResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<List<ExpenseResponse>>> call,
                    @NonNull Response<ApiResponse<List<ExpenseResponse>>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onResult(ListResult.error(
                            "Unable to load expenses (HTTP " + response.code() + ")."
                    ));
                    return;
                }
                callback.onResult(normalizeList(response.body()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<List<ExpenseResponse>>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onResult(ListResult.error("Unable to load expenses."));
                }
            }
        });
    }

    public void loadExpense(long id, ItemResultCallback callback) {
        expenseApi.getById(id).enqueue(new Callback<ApiResponse<ExpenseResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<ExpenseResponse>> call,
                    @NonNull Response<ApiResponse<ExpenseResponse>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onResult(ItemResult.error(
                            "Unable to load the expense (HTTP " + response.code() + ")."
                    ));
                    return;
                }
                callback.onResult(normalizeItem(response.body()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<ExpenseResponse>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onResult(ItemResult.error("Unable to load the expense."));
                }
            }
        });
    }

    public void createExpense(ExpenseRequest request, ItemResultCallback callback) {
        expenseApi.create(request).enqueue(new Callback<ApiResponse<ExpenseResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<ExpenseResponse>> call,
                    @NonNull Response<ApiResponse<ExpenseResponse>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onResult(ItemResult.error(
                            "Unable to record the expense (HTTP " + response.code() + ")."
                    ));
                    return;
                }
                callback.onResult(normalizeItem(response.body()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<ExpenseResponse>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onResult(ItemResult.error("Unable to record the expense."));
                }
            }
        });
    }

    public void postExpense(long id, ItemResultCallback callback) {
        expenseApi.post(id).enqueue(new Callback<ApiResponse<ExpenseResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<ExpenseResponse>> call,
                    @NonNull Response<ApiResponse<ExpenseResponse>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onResult(ItemResult.error(
                            "Unable to post the expense (HTTP " + response.code() + ")."
                    ));
                    return;
                }
                callback.onResult(normalizeItem(response.body()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<ExpenseResponse>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onResult(ItemResult.error("Unable to post the expense."));
                }
            }
        });
    }

    public void cancelExpense(long id, String reason, ItemResultCallback callback) {
        expenseApi.cancel(id, new ExpenseCancelRequest(reason)).enqueue(
                new Callback<ApiResponse<ExpenseResponse>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<ApiResponse<ExpenseResponse>> call,
                            @NonNull Response<ApiResponse<ExpenseResponse>> response
                    ) {
                        if (!response.isSuccessful()) {
                            callback.onResult(ItemResult.error(
                                    "Unable to cancel the expense (HTTP " + response.code() + ")."
                            ));
                            return;
                        }
                        callback.onResult(normalizeItem(response.body()));
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<ApiResponse<ExpenseResponse>> call,
                            @NonNull Throwable throwable
                    ) {
                        if (!call.isCanceled()) {
                            callback.onResult(ItemResult.error("Unable to cancel the expense."));
                        }
                    }
                }
        );
    }

    public void loadExpenseAccounts(AccountListCallback callback) {
        accountApi.getByType("EXPENSE").enqueue(new Callback<ApiResponse<List<AccountResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<List<AccountResponse>>> call,
                    @NonNull Response<ApiResponse<List<AccountResponse>>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onResult(AccountListResult.error(
                            "Unable to load expense accounts (HTTP " + response.code() + ")."
                    ));
                    return;
                }
                ApiResponse<List<AccountResponse>> body = response.body();
                if (body == null || !body.isSuccess()) {
                    callback.onResult(AccountListResult.error("Expense accounts could not be loaded."));
                    return;
                }
                callback.onResult(AccountListResult.success(body.getData()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<List<AccountResponse>>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onResult(AccountListResult.error("Unable to load expense accounts."));
                }
            }
        });
    }

    public void loadPaymentAccounts(AccountListCallback callback) {
        // Cash/Bank/mobile-wallet accounts live under ASSET in this chart of accounts.
        accountApi.getByType("ASSET").enqueue(new Callback<ApiResponse<List<AccountResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<List<AccountResponse>>> call,
                    @NonNull Response<ApiResponse<List<AccountResponse>>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onResult(AccountListResult.error(
                            "Unable to load payment accounts (HTTP " + response.code() + ")."
                    ));
                    return;
                }
                ApiResponse<List<AccountResponse>> body = response.body();
                if (body == null || !body.isSuccess()) {
                    callback.onResult(AccountListResult.error("Payment accounts could not be loaded."));
                    return;
                }
                callback.onResult(AccountListResult.success(body.getData()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<List<AccountResponse>>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onResult(AccountListResult.error("Unable to load payment accounts."));
                }
            }
        });
    }

    public void loadCostCenters(CostCenterListCallback callback) {
        costCenterApi.getAll().enqueue(new Callback<ApiResponse<List<CostCenterResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<List<CostCenterResponse>>> call,
                    @NonNull Response<ApiResponse<List<CostCenterResponse>>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onResult(CostCenterListResult.error(
                            "Unable to load cost centers (HTTP " + response.code() + ")."
                    ));
                    return;
                }
                ApiResponse<List<CostCenterResponse>> body = response.body();
                if (body == null || !body.isSuccess()) {
                    callback.onResult(CostCenterListResult.error("Cost centers could not be loaded."));
                    return;
                }
                callback.onResult(CostCenterListResult.success(body.getData()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<List<CostCenterResponse>>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onResult(CostCenterListResult.error("Unable to load cost centers."));
                }
            }
        });
    }

    public void loadParties(PartyListCallback callback) {
        partyApi.getAll().enqueue(new Callback<ApiResponse<List<PartyResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<List<PartyResponse>>> call,
                    @NonNull Response<ApiResponse<List<PartyResponse>>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onResult(PartyListResult.error(
                            "Unable to load parties (HTTP " + response.code() + ")."
                    ));
                    return;
                }
                ApiResponse<List<PartyResponse>> body = response.body();
                if (body == null || !body.isSuccess()) {
                    callback.onResult(PartyListResult.error("Parties could not be loaded."));
                    return;
                }
                callback.onResult(PartyListResult.success(body.getData()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<List<PartyResponse>>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onResult(PartyListResult.error("Unable to load parties."));
                }
            }
        });
    }

    static ListResult normalizeList(ApiResponse<List<ExpenseResponse>> body) {
        if (body == null) {
            return ListResult.error("The server returned an empty expense response.");
        }
        if (!body.isSuccess()) {
            String message = body.getMessage();
            return ListResult.error(message == null || message.trim().isEmpty()
                    ? "Expenses could not be loaded."
                    : message);
        }
        return ListResult.success(body.getData());
    }

    static ItemResult normalizeItem(ApiResponse<ExpenseResponse> body) {
        if (body == null) {
            return ItemResult.error("The server returned an empty expense response.");
        }
        if (!body.isSuccess()) {
            String message = body.getMessage();
            return ItemResult.error(message == null || message.trim().isEmpty()
                    ? "The expense could not be loaded."
                    : message);
        }
        if (body.getData() == null) {
            return ItemResult.error("The server returned no expense data.");
        }
        return ItemResult.success(body.getData());
    }
}