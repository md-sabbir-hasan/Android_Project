package com.nexaerp.mobile.feature.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nexaerp.mobile.databinding.ItemPickerBinding;

import java.util.ArrayList;
import java.util.List;

public class PickerAdapter extends RecyclerView.Adapter<PickerAdapter.ViewHolder> {

    public interface OnPickListener {
        void onPicked(PickerItem item);
    }

    private final List<PickerItem> items = new ArrayList<>();
    private final OnPickListener listener;

    public PickerAdapter(OnPickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<PickerItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPickerBinding binding = ItemPickerBinding.inflate(
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
        private final ItemPickerBinding binding;

        ViewHolder(ItemPickerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PickerItem item, OnPickListener listener) {
            binding.pickerTitle.setText(item.getTitle());
            binding.pickerSubtitle.setVisibility(
                    item.getSubtitle() == null || item.getSubtitle().trim().isEmpty()
                            ? View.GONE : View.VISIBLE
            );
            binding.pickerSubtitle.setText(item.getSubtitle());
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onPicked(item);
            });
        }
    }
}