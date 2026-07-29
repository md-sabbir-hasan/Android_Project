package com.nexaerp.mobile.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nexaerp.mobile.data.remote.model.ApiResponse;
import com.nexaerp.mobile.data.remote.model.dashboard.DashboardSummaryResponse;

import org.junit.Test;

public class DashboardRepositoryTest {
    @Test
    public void successFalseUsesBackendMessage() {
        ApiResponse<DashboardSummaryResponse> envelope = new ApiResponse<>();
        envelope.setSuccess(false);
        envelope.setMessage("Dashboard access denied");

        DashboardRepository.Result result = DashboardRepository.normalize(envelope);

        assertFalse(result.isSuccess());
        assertEquals("Dashboard access denied", result.getErrorMessage());
        assertTrue(result.isRetryable());
    }

    @Test
    public void successfulEnvelopeWithNullDataIsError() {
        ApiResponse<DashboardSummaryResponse> envelope = new ApiResponse<>();
        envelope.setSuccess(true);

        DashboardRepository.Result result = DashboardRepository.normalize(envelope);

        assertFalse(result.isSuccess());
        assertEquals("The server returned no dashboard data.", result.getErrorMessage());
    }
}
