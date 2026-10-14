package com.finance.api.domain.transaction.dto;

import com.finance.api.domain.transaction.RecurringFrequency;
import com.finance.api.domain.transaction.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringTransactionRequestDTO(
        @NotBlank(message = "A descrição é obrigatória")
        String description,
        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        BigDecimal amount,
        @NotNull(message = "O tipo é obrigatório")
        TransactionType type,
        @NotNull(message = "A frequência é obrigatória")
        RecurringFrequency frequency,
        @NotNull(message = "A data de início é obrigatória")
        LocalDate startDate,
        LocalDate endDate,
        @NotBlank(message = "A categoria é obrigatória")
        String categoryId,
        String accountId,
        String creditCardId
) {}