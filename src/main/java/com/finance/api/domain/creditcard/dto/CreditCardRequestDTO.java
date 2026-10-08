package com.finance.api.domain.creditcard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreditCardRequestDTO(
        @NotBlank String name,
        String accountId, // Opcional: a conta de onde a fatura costuma ser debitada
        @NotNull @Positive BigDecimal creditLimit,
        @NotNull Integer closingDay,
        @NotNull Integer dueDay,
        String color
) {}