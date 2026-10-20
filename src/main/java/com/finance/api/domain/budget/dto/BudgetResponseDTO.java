package com.finance.api.domain.budget.dto;

import com.finance.api.domain.budget.Budget;
import java.math.BigDecimal;

public record BudgetResponseDTO(
        String id,
        String categoryId,
        String categoryName,
        Integer month,
        Integer year,
        BigDecimal amount,
        BigDecimal spentAmount,
        BigDecimal remainingAmount,
        BigDecimal percentage
) {
    public BudgetResponseDTO(Budget budget, BigDecimal spentAmount) {
        this(
                budget.getId(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getMonth(),
                budget.getYear(),
                budget.getAmount(),
                spentAmount,
                budget.getAmount().subtract(spentAmount),
                budget.getAmount().signum() == 0
                        ? BigDecimal.ZERO
                        : spentAmount.multiply(BigDecimal.valueOf(100)).divide(budget.getAmount(), 2, java.math.RoundingMode.HALF_UP)
        );
    }
}
