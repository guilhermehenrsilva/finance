package com.finance.api.domain.goal.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialGoalRequestDTO(
        @NotBlank(message = "O nome da meta é obrigatório") String name,
        @NotNull @Positive(message = "O valor-alvo deve ser maior que zero") BigDecimal targetAmount,
        @FutureOrPresent(message = "O prazo não pode estar no passado") LocalDate deadline
) {}
