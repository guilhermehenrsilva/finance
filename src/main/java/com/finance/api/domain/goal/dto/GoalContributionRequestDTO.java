package com.finance.api.domain.goal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record GoalContributionRequestDTO(
        @NotNull @Positive(message = "A contribuição deve ser maior que zero") BigDecimal amount
) {}
