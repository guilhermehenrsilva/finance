package com.finance.api.domain.transfer.dto;

import com.finance.api.domain.transfer.Transfer;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransferResponseDTO(
        String id,
        String sourceAccountId,
        String sourceAccountName,
        String destinationAccountId,
        String destinationAccountName,
        BigDecimal amount,
        LocalDate date,
        String description
) {
    public TransferResponseDTO(Transfer transfer) {
        this(
                transfer.getId(),
                transfer.getSourceAccount().getId(),
                transfer.getSourceAccount().getName(),
                transfer.getDestinationAccount().getId(),
                transfer.getDestinationAccount().getName(),
                transfer.getAmount(),
                transfer.getDate(),
                transfer.getDescription()
        );
    }
}
