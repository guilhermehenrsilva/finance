package com.finance.api.domain.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AccountRequestDTO(
        @NotBlank String name,
        @NotBlank String type,
        @NotNull BigDecimal initialBalance,
        String color
) {}