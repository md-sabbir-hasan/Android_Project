package com.nexaerp.mobile.data.repository;

import androidx.annotation.NonNull;

import com.nexaerp.mobile.data.remote.api.PermissionApi;
import com.nexaerp.mobile.data.remote.api.RoleApi;
import com.nexaerp.mobile.data.remote.model.ApiResponse;
import com.nexaerp.mobile.data.remote.model.role.PermissionResponse;
import com.nexaerp.mobile.data.remote.model.role.RoleRequest;
import com.nexaerp.mobile.data.remote.model.role.RoleResponse;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class RoleRepository {
    public interface ListResultCallback {
        void onResult(ListResult result);
    }

    public interface PermissionListResultCallback {
        void onResult(PermissionListResult result);
    }

    public interface ItemResultCallback {
        void onResult(ItemResult result);
    }

    public static final class ListResult {
        private final List<RoleResponse> items;
        private final String errorMessage;

        private ListResult(List<RoleResponse> items, String errorMessage) {
            this.items = items;
            this.errorMessage = errorMessage;
        }

        public static ListResult success(List<RoleResponse> items) {
            return new ListResult(items == null ? Collections.emptyList() : items, null);
        }

        public static ListResult error(String errorMessage) {
            return new ListResult(null, errorMessage);
        }

        public boolean isSuccess() { return items != null; }
        public List<RoleResponse> getItems() { return items; }
        public String getErrorMessage() { return errorMessage; }
    }

    public static final class PermissionListResult {
        private final List<PermissionResponse> items;
        private final String errorMessage;

        private PermissionListResult(List<PermissionResponse> items, String errorMessage) {
            this.items = items;
            this.errorMessage = errorMessage;
        }

        public static PermissionListResult success(List<PermissionResponse> items) {
            return new PermissionListResult(items == null ? Collections.emptyList() : items, null);
        }

        public static PermissionListResult error(String errorMessage) {
            return new PermissionListResult(null, errorMessage);
        }

        public boolean isSuccess() { return items != null; }
        public List<PermissionResponse> getItems() { return items; }
        public String getErrorMessage() { return errorMessage; }
    }

    public static final class ItemResult {
        private final RoleResponse item;
        private final String errorMessage;

        private ItemResult(RoleResponse item, String errorMessage) {
            this.item = item;
            this.errorMessage = errorMessage;
        }

        public static ItemResult success(RoleResponse item) {
            return new ItemResult(item, null);
        }

        public static ItemResult error(String errorMessage) {
            return new ItemResult(null, errorMessage);
        }

        public boolean isSuccess() { return item != null; }
        public RoleResponse getItem() { return item; }
        public String getErrorMessage() { return errorMessage; }
    }

    private final RoleApi roleApi;
    private final PermissionApi permissionApi;

    public RoleRepository(RoleApi roleApi, PermissionApi permissionApi) {
        this.roleApi = roleApi;
        this.permissionApi = permissionApi;
    }

    public void loadRoles(ListResultCallback callback) {
        roleApi.getAll().enqueue(new Callback<ApiResponse<List<RoleResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<List<RoleResponse>>> call,
                    @NonNull Response<ApiResponse<List<RoleResponse>>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onResult(ListResult.error(
                            "Unable to load roles (HTTP " + response.code() + ")."
                    ));
                    return;
                }
                callback.onResult(normalizeList(response.body()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<List<RoleResponse>>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onResult(ListResult.error("Unable to load roles."));
                }
            }
        });
    }

    public void loadRole(long id, ItemResultCallback callback) {
        roleApi.getById(id).enqueue(new Callback<ApiResponse<RoleResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<RoleResponse>> call,
                    @NonNull Response<ApiResponse<RoleResponse>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onResult(ItemResult.error(
                            "Unable to load the role (HTTP " + response.code() + ")."
                    ));
                    return;
                }
                callback.onResult(normalizeItem(response.body()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<RoleResponse>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onResult(ItemResult.error("Unable to load the role."));
                }
            }
        });
    }

    public void loadPermissions(PermissionListResultCallback callback) {
        permissionApi.getAll().enqueue(new Callback<ApiResponse<List<PermissionResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<List<PermissionResponse>>> call,
                    @NonNull Response<ApiResponse<List<PermissionResponse>>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onResult(PermissionListResult.error(
                            "Unable to load permissions (HTTP " + response.code() + ")."
                    ));
                    return;
                }
                callback.onResult(normalizePermissionList(response.body()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<List<PermissionResponse>>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onResult(PermissionListResult.error("Unable to load permissions."));
                }
            }
        });
    }

    public void createRole(RoleRequest request, ItemResultCallback callback) {
        roleApi.create(request).enqueue(new Callback<ApiResponse<RoleResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<RoleResponse>> call,
                    @NonNull Response<ApiResponse<RoleResponse>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onResult(ItemResult.error(
                            "Unable to create the role (HTTP " + response.code() + ")."
                    ));
                    return;
                }
                callback.onResult(normalizeItem(response.body()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<RoleResponse>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onResult(ItemResult.error("Unable to create the role."));
                }
            }
        });
    }

    public void updateRole(long id, RoleRequest request, ItemResultCallback callback) {
        roleApi.update(id, request).enqueue(new Callback<ApiResponse<RoleResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<RoleResponse>> call,
                    @NonNull Response<ApiResponse<RoleResponse>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onResult(ItemResult.error(
                            "Unable to update the role (HTTP " + response.code() + ")."
                    ));
                    return;
                }
                callback.onResult(normalizeItem(response.body()));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<RoleResponse>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onResult(ItemResult.error("Unable to update the role."));
                }
            }
        });
    }

    public void cancel() {
        // no-op placeholder to mirror other repositories' lifecycle hook
    }

    static ListResult normalizeList(ApiResponse<List<RoleResponse>> body) {
        if (body == null) {
            return ListResult.error("The server returned an empty role response.");
        }
        if (!body.isSuccess()) {
            String message = body.getMessage();
            return ListResult.error(message == null || message.trim().isEmpty()
                    ? "Roles could not be loaded."
                    : message);
        }
        return ListResult.success(body.getData());
    }

    static PermissionListResult normalizePermissionList(ApiResponse<List<PermissionResponse>> body) {
        if (body == null) {
            return PermissionListResult.error("The server returned an empty permission response.");
        }
        if (!body.isSuccess()) {
            String message = body.getMessage();
            return PermissionListResult.error(message == null || message.trim().isEmpty()
                    ? "Permissions could not be loaded."
                    : message);
        }
        return PermissionListResult.success(body.getData());
    }

    static ItemResult normalizeItem(ApiResponse<RoleResponse> body) {
        if (body == null) {
            return ItemResult.error("The server returned an empty role response.");
        }
        if (!body.isSuccess()) {
            String message = body.getMessage();
            return ItemResult.error(message == null || message.trim().isEmpty()
                    ? "The role could not be loaded."
                    : message);
        }
        if (body.getData() == null) {
            return ItemResult.error("The server returned no role data.");
        }
        return ItemResult.success(body.getData());
    }
}