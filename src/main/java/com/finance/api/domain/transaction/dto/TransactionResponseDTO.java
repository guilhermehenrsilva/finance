package com.finance.api.domain.transaction.dto;

import com.finance.api.domain.transaction.Transaction;
import com.finance.api.domain.transaction.TransactionStatus;
import com.finance.api.domain.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponseDTO(
        String id,
        String accountId,
        String accountName,
        String description,
        BigDecimal amount,
        TransactionType type,
        TransactionStatus status,
        LocalDate date
) {
    public TransactionResponseDTO(Transaction t) {
        this(
                t.getId(),
                t.getAccount().getId(),
                t.getAccount().getName(),
                t.getDescription(),
                t.getAmount(),
                t.getType(),
                t.getStatus(),
                t.getDate()
        );
    }
}