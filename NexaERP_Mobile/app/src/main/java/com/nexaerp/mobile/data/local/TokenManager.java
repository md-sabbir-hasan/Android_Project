package com.nexaerp.mobile.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.nexaerp.mobile.data.remote.model.auth.LoginResponse;

public class TokenManager {

    private static final String PREFERENCE_NAME = "nexa_auth";
    private static final String KEY_ACCESS_TOKEN = "accessToken";
    private static final String KEY_REFRESH_TOKEN = "refreshToken";
    private static final String KEY_EXPIRES_IN = "expiresIn";
    private static final String KEY_ACCESS_TOKEN_EXPIRES_AT = "accessTokenExpiresAt";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final long EXPIRY_SAFETY_WINDOW_MS = 30_000L;

    private final SharedPreferences preferences;

    public TokenManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(
                PREFERENCE_NAME,
                Context.MODE_PRIVATE
        );
    }

    public synchronized void saveLoginSession(LoginResponse response) {
        if (response == null) {
            return;
        }

        SharedPreferences.Editor editor = preferences.edit().clear();
        putNullableString(editor, KEY_ACCESS_TOKEN, response.getAccessToken());
        putNullableString(editor, KEY_REFRESH_TOKEN, response.getRefreshToken());
        storeExpiry(editor, response.getExpiresIn());

        if (response.getUserId() == null) {
            editor.remove(KEY_USER_ID);
        } else {
            editor.putLong(KEY_USER_ID, response.getUserId());
        }

        putNullableString(editor, KEY_NAME, response.getName());
        putNullableString(editor, KEY_EMAIL, response.getEmail());
        editor.commit();
    }

    public synchronized void updateAccessToken(LoginResponse response) {
        if (response == null || isBlank(response.getAccessToken())) {
            return;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_ACCESS_TOKEN, response.getAccessToken());
        if (!isBlank(response.getRefreshToken())) {
            editor.putString(KEY_REFRESH_TOKEN, response.getRefreshToken());
        }
        storeExpiry(editor, response.getExpiresIn());
        if (response.getUserId() != null) {
            editor.putLong(KEY_USER_ID, response.getUserId());
        }
        putNullableString(editor, KEY_NAME, response.getName());
        putNullableString(editor, KEY_EMAIL, response.getEmail());
        editor.commit();
    }

    public String getAccessToken() {
        return preferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return preferences.getString(KEY_REFRESH_TOKEN, null);
    }

    public long getExpiresIn() {
        return preferences.getLong(KEY_EXPIRES_IN, 0L);
    }

    public long getAccessTokenExpiresAt() {
        return preferences.getLong(KEY_ACCESS_TOKEN_EXPIRES_AT, 0L);
    }

    public Long getUserId() {
        return preferences.contains(KEY_USER_ID)
                ? preferences.getLong(KEY_USER_ID, 0L)
                : null;
    }

    public String getName() {
        return preferences.getString(KEY_NAME, null);
    }

    public String getEmail() {
        return preferences.getString(KEY_EMAIL, null);
    }

    public boolean hasAccessToken() {
        return !isBlank(getAccessToken());
    }

    public boolean hasRefreshToken() {
        return !isBlank(getRefreshToken());
    }

    public boolean isAccessTokenExpired() {
        long expiresAt = getAccessTokenExpiresAt();
        return !hasAccessToken() || expiresAt <= 0L || System.currentTimeMillis() >= expiresAt;
    }

    public boolean isAccessTokenExpiringSoon() {
        long expiresAt = getAccessTokenExpiresAt();
        return !hasAccessToken()
                || expiresAt <= 0L
                || System.currentTimeMillis() >= expiresAt - EXPIRY_SAFETY_WINDOW_MS;
    }

    public synchronized void clearSession() {
        preferences.edit().clear().commit();
    }

    private void storeExpiry(SharedPreferences.Editor editor, Long expiresInMs) {
        if (expiresInMs == null || expiresInMs <= 0L) {
            editor.putLong(KEY_EXPIRES_IN, 0L);
            editor.remove(KEY_ACCESS_TOKEN_EXPIRES_AT);
            return;
        }
        long now = System.currentTimeMillis();
        long expiresAt = expiresInMs > Long.MAX_VALUE - now
                ? Long.MAX_VALUE
                : now + expiresInMs;
        editor.putLong(KEY_EXPIRES_IN, expiresInMs);
        editor.putLong(KEY_ACCESS_TOKEN_EXPIRES_AT, expiresAt);
    }

    private void putNullableString(SharedPreferences.Editor editor, String key, String value) {
        if (value == null) {
            editor.remove(key);
        } else {
            editor.putString(key, value);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}