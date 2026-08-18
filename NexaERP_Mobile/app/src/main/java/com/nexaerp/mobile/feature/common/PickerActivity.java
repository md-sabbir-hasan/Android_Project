package com.nexaerp.mobile.feature.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nexaerp.mobile.databinding.ActivityPickerBinding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Generic single-select picker. The caller supplies the full item list up front
 * (no network calls happen inside this screen) and receives the chosen
 * {@link PickerItem} back via {@link #EXTRA_RESULT_ITEM}.
 */
public class PickerActivity extends AppCompatActivity {

    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_ITEMS = "extra_items";
    public static final String EXTRA_RESULT_ITEM = "extra_result_item";

    private ActivityPickerBinding binding;
    private PickerAdapter adapter;
    private List<PickerItem> allItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        binding.toolbar.setTitle(title);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        @SuppressWarnings("unchecked")
        List<PickerItem> items = (List<PickerItem>) getIntent().getSerializableExtra(EXTRA_ITEMS);
        allItems = items == null ? new ArrayList<>() : items;

        adapter = new PickerAdapter(item -> {
            Intent result = new Intent();
            result.putExtra(EXTRA_RESULT_ITEM, item);
            setResult(Activity.RESULT_OK, result);
            finish();
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        filter("");
    }

    private void filter(String query) {
        String needle = query.trim().toLowerCase(Locale.getDefault());
        List<PickerItem> filtered = new ArrayList<>();
        for (PickerItem item : allItems) {
            String title = item.getTitle() == null ? "" : item.getTitle().toLowerCase(Locale.getDefault());
            String subtitle = item.getSubtitle() == null ? "" : item.getSubtitle().toLowerCase(Locale.getDefault());
            if (needle.isEmpty() || title.contains(needle) || subtitle.contains(needle)) {
                filtered.add(item);
            }
        }
        adapter.submitList(filtered);
        binding.emptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }

    public static Intent newIntent(Activity activity, String title, ArrayList<PickerItem> items) {
        Intent intent = new Intent(activity, PickerActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_ITEMS, (Serializable) items);
        return intent;
    }
}