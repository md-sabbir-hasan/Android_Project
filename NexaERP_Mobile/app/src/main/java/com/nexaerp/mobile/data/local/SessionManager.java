package com.nexaerp.mobile.data.local;

import android.os.Handler;
import android.os.Looper;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class SessionManager {

    public interface Listener {
        void onSessionExpired();
    }

    private static final SessionManager INSTANCE = new SessionManager();

    private final Set<Listener> listeners = new CopyOnWriteArraySet<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void registerListener(Listener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void unregisterListener(Listener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public void notifySessionExpired() {
        mainHandler.post(() -> {
            for (Listener listener : listeners) {
                listener.onSessionExpired();
            }
        });
    }
}