package com.finance.api.domain.goal.dto;

import com.finance.api.domain.goal.FinancialGoal;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record FinancialGoalResponseDTO(
        String id,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        BigDecimal remainingAmount,
        BigDecimal percentage,
        LocalDate deadline,
        boolean completed
) {
    public FinancialGoalResponseDTO(FinancialGoal goal) {
        this(
                goal.getId(),
                goal.getName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getTargetAmount().subtract(goal.getCurrentAmount()).max(BigDecimal.ZERO),
                goal.getTargetAmount().signum() == 0 ? BigDecimal.ZERO :
                        goal.getCurrentAmount().multiply(BigDecimal.valueOf(100))
                                .divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP),
                goal.getDeadline(),
                goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0
        );
    }
}
