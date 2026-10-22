package com.finance.api.domain.account.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record AccountUpdateDTO(
        @NotBlank String name,
        @NotBlank String type,
        BigDecimal initialBalance,
        String color,
        Boolean active
) {}
