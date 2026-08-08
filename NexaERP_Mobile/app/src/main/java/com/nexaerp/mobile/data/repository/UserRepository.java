package com.nexaerp.mobile.data.repository;

import androidx.annotation.NonNull;

import com.nexaerp.mobile.data.remote.api.UserApi;
import com.nexaerp.mobile.data.remote.model.ApiResponse;
import com.nexaerp.mobile.data.remote.model.PageResponse;
import com.nexaerp.mobile.data.remote.model.user.UserRequest;
import com.nexaerp.mobile.data.remote.model.user.UserResponse;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class UserRepository {
    public interface PageResultCallback {
        void onResult(PageResult result);
    }

    public interface ItemResultCallback {
        void onResult(ItemResult result);
    }

    public interface VoidResultCallback {
        void onResult(VoidResult result);
    }

    public static final class PageResult {
        private final List<UserResponse> items;
        private final boolean last;
        private final String errorMessage;

        private PageResult(List<UserResponse> items, boolean last, String errorMessage) {
            this.items = items;
            this.last = last;
            this.errorMessage = errorMessage;
        }

        public static PageResult success(List<UserResponse> items, boolean last) {
            return new PageResult(items == null ? Collections.emptyList() : items, last, null);
        }

        public static PageResult error(String errorMessage) {
            return new PageResult(null, true, errorMessage);
        }

        public boolean isSuccess() { return items != null; }
        public List<UserResponse> getItems() { return items; }
        public boolean isLast() { return last; }
        public String getErrorMessage() { return errorMessage; }
    }

    public static final class ItemResult {
        private final UserResponse item;
        private final String errorMessage;

        private ItemResult(UserResponse item, String errorMessage) {
            this.item = item;
            this.errorMessage = errorMessage;
        }

        public static ItemResult success(UserResponse item) {
            return new ItemResult(item, null);
        }

        public static ItemResult error(String errorMessage) {
            return new ItemResult(null, errorMessage);
        }

        public boolean isSuccess() { return item != null; }
        public UserResponse getItem() { return item; }
        public String getErrorMessage() { return errorMessage; }
    }

    public static final class VoidResult {
        private final boolean success;
        private final String errorMessage;

        private VoidResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public static VoidResult success() { return new VoidResult(true, null); }
        public static VoidResult error(String errorMessage) { return new VoidResult(false, errorMessage); }

        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
    }

    private final UserApi userApi;
    private Call<ApiResponse<PageResponse<UserResponse>>> pageCall;

    public UserRepository(UserApi userApi) {
        this.userApi = userApi;
    }

    public void loadUsers(
            int page,
            int size,
            String search,
            String status,
            PageResultCallback callback
    ) {
        if (pageCall != null) pageCall.cancel();
        pageCall = userApi.getAll(page, size, search, status);
        pageCall.enqueue(new Callback<ApiResponse<PageResponse<UserResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<PageResponse<UserResponse>>> call,
                    @NonNull Response<ApiResponse<PageResponse<UserResponse>>> response
            ) {
                pageCall = null;
                if (!response.isSuccessful()) {
                    callback.onResult(PageResult.error(
                            "Unable to load users (HTTP " + response.code() + ")."
                    ));
                    return;
                }
                callback.onResult(normalizePage(response.body()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<PageResponse<UserResponse>>> call,
                    @NonNull Throwable throwable
            ) {
                pageCall = null;
                if (!call.isCanceled()) {
                    callback.onResult(PageResult.error("Unable to load users."));
                }
            }
        });
    }

    public void loadUser(long id, ItemResultCallback callback) {
        userApi.getById(id).enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<UserResponse>> call,
                    @NonNull Response<ApiResponse<UserResponse>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onResult(ItemResult.error(
                            "Unable to load the user (HTTP " + response.code() + ")."
                    ));
                    return;
                }
                callback.onResult(normalizeItem(response.body()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<UserResponse>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onResult(ItemResult.error("Unable to load the user."));
                }
            }
        });
    }

    public void createUser(UserRequest request, ItemResultCallback callback) {
        userApi.create(request).enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<UserResponse>> call,
                    @NonNull Response<ApiResponse<UserResponse>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onResult(ItemResult.error(
                            "Unable to create the user (HTTP " + response.code() + ")."
                    ));
                    return;
                }
                callback.onResult(normalizeItem(response.body()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<UserResponse>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onResult(ItemResult.error("Unable to create the user."));
                }
            }
        });
    }

    public void updateUser(long id, UserRequest request, ItemResultCallback callback) {
        userApi.update(id, request).enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<UserResponse>> call,
                    @NonNull Response<ApiResponse<UserResponse>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onResult(ItemResult.error(
                            "Unable to update the user (HTTP " + response.code() + ")."
                    ));
                    return;
                }
                callback.onResult(normalizeItem(response.body()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<UserResponse>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onResult(ItemResult.error("Unable to update the user."));
                }
            }
        });
    }

    public void activateUser(long id, VoidResultCallback callback) {
        toggleStatus(userApi.activate(id), "Unable to activate the user.", callback);
    }

    public void deactivateUser(long id, VoidResultCallback callback) {
        toggleStatus(userApi.deactivate(id), "Unable to deactivate the user.", callback);
    }

    private void toggleStatus(
            Call<ApiResponse<Void>> call,
            String genericErrorMessage,
            VoidResultCallback callback
    ) {
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<Void>> call,
                    @NonNull Response<ApiResponse<Void>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onResult(VoidResult.error(
                            genericErrorMessage + " (HTTP " + response.code() + ")."
                    ));
                    return;
                }
                ApiResponse<Void> body = response.body();
                if (body == null || !body.isSuccess()) {
                    String message = body == null ? null : body.getMessage();
                    callback.onResult(VoidResult.error(
                            message == null || message.trim().isEmpty()
                                    ? genericErrorMessage
                                    : message
                    ));
                    return;
                }
                callback.onResult(VoidResult.success());
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<Void>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onResult(VoidResult.error(genericErrorMessage));
                }
            }
        });
    }

    public void cancel() {
        if (pageCall != null) {
            pageCall.cancel();
            pageCall = null;
        }
    }

    static PageResult normalizePage(ApiResponse<PageResponse<UserResponse>> body) {
        if (body == null) {
            return PageResult.error("The server returned an empty user response.");
        }
        if (!body.isSuccess()) {
            String message = body.getMessage();
            return PageResult.error(message == null || message.trim().isEmpty()
                    ? "Users could not be loaded."
                    : message);
        }
        PageResponse<UserResponse> page = body.getData();
        if (page == null) {
            return PageResult.error("The server returned no user page.");
        }
        return PageResult.success(page.getContent(), page.isLast());
    }

    static ItemResult normalizeItem(ApiResponse<UserResponse> body) {
        if (body == null) {
            return ItemResult.error("The server returned an empty user response.");
        }
        if (!body.isSuccess()) {
            String message = body.getMessage();
            return ItemResult.error(message == null || message.trim().isEmpty()
                    ? "The user could not be loaded."
                    : message);
        }
        if (body.getData() == null) {
            return ItemResult.error("The server returned no user data.");
        }
        return ItemResult.success(body.getData());
    }
}