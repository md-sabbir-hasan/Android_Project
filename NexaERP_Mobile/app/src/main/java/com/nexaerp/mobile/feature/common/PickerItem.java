package com.nexaerp.mobile.feature.common;

import java.io.Serializable;

public final class PickerItem implements Serializable {
    private final long id;
    private final String title;
    private final String subtitle;

    public PickerItem(long id, String title, String subtitle) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
}