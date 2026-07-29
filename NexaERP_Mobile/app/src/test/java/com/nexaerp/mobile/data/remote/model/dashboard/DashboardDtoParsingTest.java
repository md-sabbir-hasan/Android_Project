package com.nexaerp.mobile.data.remote.model.dashboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nexaerp.mobile.data.remote.client.LocalDateAdapter;
import com.nexaerp.mobile.data.remote.client.LocalDateTimeAdapter;
import com.nexaerp.mobile.data.remote.model.ApiResponse;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.lang.reflect.Type;

public class DashboardDtoParsingTest {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    @Test
    public void parsesMoneyDatesAndNullableSections() {
        String json = "{\"success\":true,\"message\":\"Dashboard summary loaded\",\"data\":{"
                + "\"users\":null,\"security\":null,\"finance\":null,"
                + "\"business\":{\"cashPosition\":1234.50,\"cashConfigured\":true,"
                + "\"asOfDate\":\"2026-07-29\",\"currencyCode\":\"BDT\","
                + "\"accountsReceivable\":null,\"overdueInvoiceCount\":2},"
                + "\"system\":{\"serverTime\":\"2026-07-29T10:15:30\"},"
                + "\"recentActivities\":null,\"budget\":null,"
                + "\"expense\":{\"draftCount\":3,\"draftTotalAmount\":75.25,"
                + "\"postedThisMonthTotal\":500,\"recurringActiveCount\":0,"
                + "\"recurringDueSoonCount\":0,\"outstandingDue\":10}}}";

        Type type = new TypeToken<ApiResponse<DashboardSummaryResponse>>() {}.getType();
        ApiResponse<DashboardSummaryResponse> envelope = gson.fromJson(json, type);
        DashboardSummaryResponse response = envelope.getData();

        assertNull(response.getUsers());
        assertNotNull(response.getBusiness());
        assertEquals(new BigDecimal("1234.50"), response.getBusiness().getCashPosition());
        assertEquals(LocalDate.of(2026, 7, 29), response.getBusiness().getAsOfDate());
        assertEquals(LocalDateTime.of(2026, 7, 29, 10, 15, 30),
                response.getSystem().getServerTime());
        assertEquals(3L, response.getExpense().getDraftCount());
    }
}
