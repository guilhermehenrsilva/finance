package com.finance.api.domain.creditcard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record InvoicePaymentRequestDTO(
        @NotBlank(message = "A conta de pagamento é obrigatória")
        String accountId,
        @Positive(message = "O valor do pagamento deve ser maior que zero")
        BigDecimal amount
) {
}
