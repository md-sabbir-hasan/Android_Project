package com.nexaerp.mobile.feature.user;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nexaerp.mobile.R;
import com.nexaerp.mobile.data.remote.model.user.UserResponse;
import com.nexaerp.mobile.databinding.ItemUserBinding;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    public interface OnUserClickListener {
        void onUserClicked(UserResponse user);
    }

    private final List<UserResponse> items = new ArrayList<>();
    private final OnUserClickListener listener;

    public UserAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<UserResponse> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(
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
        private final ItemUserBinding binding;

        ViewHolder(ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(UserResponse user, OnUserClickListener listener) {
            Context context = binding.getRoot().getContext();
            String name = UserPresenter.safeName(user, context.getString(R.string.user_unnamed));

            binding.userName.setText(name);
            binding.userEmail.setText(user == null ? "" : user.getEmail());
            binding.userAvatarInitials.setText(UserPresenter.initials(name));

            tintOval(context, binding.userAvatar.getBackground(),
                    UserPresenter.avatarBackgroundColorRes(name));
            binding.userAvatarInitials.setTextColor(
                    ContextCompat.getColor(context, UserPresenter.avatarForegroundColorRes(name))
            );

            String status = user == null ? null : user.getStatus();
            binding.userStatusPill.setText(UserPresenter.statusLabel(context, status));
            tintPill(context, binding.userStatusPill.getBackground(),
                    UserPresenter.statusBackgroundColorRes(status));
            binding.userStatusPill.setTextColor(
                    ContextCompat.getColor(context, UserPresenter.statusForegroundColorRes(status))
            );

            binding.userRoles.setText(rolesSummary(user, context.getString(R.string.user_no_roles)));

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null && user != null) listener.onUserClicked(user);
            });
        }

        private String rolesSummary(UserResponse user, String fallback) {
            if (user == null || user.getRoles() == null || user.getRoles().isEmpty()) {
                return fallback;
            }
            return String.join(", ", user.getRoles());
        }

        private void tintOval(Context context, Drawable background, int colorRes) {
            if (background instanceof GradientDrawable) {
                ((GradientDrawable) background.mutate())
                        .setColor(ContextCompat.getColor(context, colorRes));
            }
        }

        private void tintPill(Context context, Drawable background, int colorRes) {
            if (background instanceof GradientDrawable) {
                ((GradientDrawable) background.mutate())
                        .setColor(ContextCompat.getColor(context, colorRes));
            }
        }
    }
}