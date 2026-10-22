package com.finance.api.domain.transaction.dto;

import com.finance.api.domain.transaction.TransactionStatus;
import com.finance.api.domain.transaction.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TransactionRequestDTO(
        @NotBlank(message = "A descrição é obrigatória")
        String description,
        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        BigDecimal amount,
        @NotNull(message = "O tipo é obrigatório")
        TransactionType type,
        @NotNull(message = "O status é obrigatório")
        TransactionStatus status,
        @NotNull(message = "A data é obrigatória")
        LocalDate date,
        String categoryId,
        String accountId,
        String creditCardId,
        @Min(value = 1, message = "O número de parcelas deve ser maior que zero")
        Integer totalInstallments,
        List<String> tagIds
) {
    public TransactionRequestDTO(
            String description,
            BigDecimal amount,
            TransactionType type,
            TransactionStatus status,
            LocalDate date,
            String categoryId,
            String accountId,
            String creditCardId,
            Integer totalInstallments
    ) {
        this(description, amount, type, status, date, categoryId, accountId, creditCardId, totalInstallments, null);
    }
}