package com.nexaerp.mobile.data.remote.client;

import android.util.Log;

import com.nexaerp.mobile.BuildConfig;

import java.util.regex.Pattern;

import okhttp3.logging.HttpLoggingInterceptor;

final class SafeHttpLogging {

    private static final Pattern SENSITIVE_JSON_FIELD = Pattern.compile(
            "(\\\"(?:accessToken|refreshToken|password)\\\"\\s*:\\s*\\\")[^\\\"]*(\\\")"
    );

    private SafeHttpLogging() {
    }

    static HttpLoggingInterceptor create() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(
                message -> Log.d("OkHttp", redact(message))
        );
        interceptor.redactHeader("Authorization");
        interceptor.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);
        return interceptor;
    }

    private static String redact(String message) {
        return SENSITIVE_JSON_FIELD.matcher(message).replaceAll("$1██$2");
    }
}