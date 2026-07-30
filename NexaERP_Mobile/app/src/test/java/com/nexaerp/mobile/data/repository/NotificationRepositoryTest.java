package com.nexaerp.mobile.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nexaerp.mobile.data.remote.model.ApiResponse;

import org.junit.Test;

import java.lang.reflect.Type;

public class NotificationRepositoryTest {
    @Test
    public void parsesScalarUnreadCountEnvelope() {
        Type type = new TypeToken<ApiResponse<Long>>() {}.getType();
        ApiResponse<Long> response = new Gson().fromJson(
                "{\"success\":true,\"message\":\"OK\",\"data\":7}",
                type
        );

        NotificationRepository.Result result = NotificationRepository.normalize(response);

        assertTrue(result.isSuccess());
        assertEquals(Long.valueOf(7L), result.getUnreadCount());
    }

    @Test
    public void rejectsNullDataAndUsesEnvelopeError() {
        ApiResponse<Long> nullData = new ApiResponse<>();
        nullData.setSuccess(true);
        assertFalse(NotificationRepository.normalize(nullData).isSuccess());

        ApiResponse<Long> failure = new ApiResponse<>();
        failure.setSuccess(false);
        failure.setMessage("Count unavailable");
        NotificationRepository.Result result = NotificationRepository.normalize(failure);
        assertFalse(result.isSuccess());
        assertEquals("Count unavailable", result.getErrorMessage());
    }
}
