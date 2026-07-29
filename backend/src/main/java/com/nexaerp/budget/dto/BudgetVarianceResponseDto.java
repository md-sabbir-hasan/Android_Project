package com.nexaerp.budget.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.nexaerp.budget.BudgetStatus;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetVarianceResponseDto {
    private Long budgetId;
    private String budgetName;
    private String budgetNumber;
    private BudgetStatus budgetStatus;
    private String currencyCode;

    private Long fiscalYearId;
    private String fiscalYearName;

    private LocalDate fromDate;
    private LocalDate toDate;

    private BigDecimal totalRevenueBudget;
    private BigDecimal totalRevenueActual;
    private BigDecimal totalRevenueVariance;
    private BigDecimal revenueAchievementPercent;
    private BigDecimal totalExpenseBudget;
    private BigDecimal totalExpenseActual;
    private BigDecimal totalExpenseVariance;
    private BigDecimal expenseUtilizationPercent;

    private LocalDateTime generatedAt;

    private List<BudgetVarianceLineDto> lines;
}
