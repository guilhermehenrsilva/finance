package com.finance.api.domain.transfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransferRequestDTO(
        @NotBlank(message = "A conta de origem é obrigatória")
        String sourceAccountId,
        @NotBlank(message = "A conta de destino é obrigatória")
        String destinationAccountId,
        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        BigDecimal amount,
        @NotNull(message = "A data é obrigatória")
        LocalDate date,
        String description
) {
}
