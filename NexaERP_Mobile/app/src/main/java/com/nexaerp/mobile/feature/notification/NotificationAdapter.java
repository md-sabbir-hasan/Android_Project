package com.nexaerp.mobile.feature.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nexaerp.mobile.data.remote.model.notification.NotificationItemResponse;
import com.nexaerp.mobile.databinding.ItemNotificationBinding;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public interface ItemClickListener {
        void onItemClicked(NotificationItemResponse item);
    }

    private final List<NotificationItemResponse> items = new ArrayList<>();
    private final ItemClickListener clickListener;

    public NotificationAdapter(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void submitList(List<NotificationItemResponse> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), clickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationBinding binding;

        ViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(NotificationItemResponse item, ItemClickListener clickListener) {
            binding.notificationTitle.setText(NotificationPresenter.title(item));

            String message = item.getMessage();
            boolean hasMessage = message != null && !message.trim().isEmpty();
            binding.notificationMessage.setVisibility(hasMessage ? View.VISIBLE : View.GONE);
            if (hasMessage) {
                binding.notificationMessage.setText(message);
            }

            binding.notificationTimestamp.setText(
                    NotificationPresenter.formatTimestamp(item.getCreatedAt())
            );
            binding.notificationTypeChip.setText(NotificationPresenter.readableType(item.getType()));
            binding.unreadDot.setVisibility(item.isRead() ? View.INVISIBLE : View.VISIBLE);
            binding.getRoot().setAlpha(item.isRead() ? 0.6f : 1f);
            binding.getRoot().setOnClickListener(view -> {
                if (clickListener != null) {
                    clickListener.onItemClicked(item);
                }
            });
        }
    }
}