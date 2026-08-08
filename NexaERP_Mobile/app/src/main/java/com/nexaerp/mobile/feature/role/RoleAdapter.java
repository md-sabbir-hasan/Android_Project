package com.nexaerp.mobile.feature.role;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nexaerp.mobile.R;
import com.nexaerp.mobile.data.remote.model.role.RoleResponse;
import com.nexaerp.mobile.databinding.ItemRoleBinding;


import java.util.ArrayList;
import java.util.List;

public class RoleAdapter extends RecyclerView.Adapter<RoleAdapter.ViewHolder> {

    public interface OnRoleClickListener {
        void onRoleClicked(RoleResponse role);
    }

    private final List<RoleResponse> items = new ArrayList<>();
    private final OnRoleClickListener listener;

    public RoleAdapter(OnRoleClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<RoleResponse> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRoleBinding binding = ItemRoleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemRoleBinding binding;

        ViewHolder(ItemRoleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RoleResponse role, OnRoleClickListener listener) {
            android.content.Context context = binding.getRoot().getContext();

            binding.roleName.setText(
                    role.getName() == null || role.getName().trim().isEmpty()
                            ? context.getString(R.string.role_unnamed)
                            : role.getName()
            );

            String description = role.getDescription();
            binding.roleDescription.setVisibility(
                    description == null || description.trim().isEmpty()
                            ? android.view.View.GONE
                            : android.view.View.VISIBLE
            );
            binding.roleDescription.setText(description);

            int permissionCount = role.getPermissions() == null ? 0 : role.getPermissions().size();
            int userCount = role.getUserCount() == null ? 0 : role.getUserCount();
            binding.roleMeta.setText(context.getString(
                    R.string.role_meta_summary, permissionCount, userCount
            ));

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onRoleClicked(role);
            });
        }
    }
}