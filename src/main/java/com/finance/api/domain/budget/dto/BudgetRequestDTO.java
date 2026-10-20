package com.finance.api.domain.budget.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record BudgetRequestDTO(
        @NotBlank(message = "A categoria é obrigatória") String categoryId,
        @NotNull @Min(value = 1) @Max(value = 12) Integer month,
        @NotNull @Min(value = 2000) Integer year,
        @NotNull @Positive(message = "O limite deve ser maior que zero") BigDecimal amount
) {}
