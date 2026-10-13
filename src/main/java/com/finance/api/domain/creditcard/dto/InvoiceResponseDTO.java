package com.finance.api.domain.creditcard.dto;

import com.finance.api.domain.creditcard.Invoice;
import com.finance.api.domain.creditcard.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceResponseDTO(
        String id,
        String creditCardId,
        String creditCardName,
        Integer month,
        Integer year,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        InvoiceStatus status,
        LocalDate dueDate
) {
    public InvoiceResponseDTO(Invoice invoice) {
        this(
                invoice.getId(),
                invoice.getCreditCard().getId(),
                invoice.getCreditCard().getName(),
                invoice.getMonth(),
                invoice.getYear(),
                invoice.getTotalAmount(),
                invoice.getPaidAmount(),
                invoice.getTotalAmount().subtract(invoice.getPaidAmount()).max(BigDecimal.ZERO),
                invoice.getStatus(),
                dueDate(invoice)
        );
    }

    private static LocalDate dueDate(Invoice invoice) {
        int day = Math.min(invoice.getCreditCard().getDueDay(),
                LocalDate.of(invoice.getYear(), invoice.getMonth(), 1).lengthOfMonth());
        return LocalDate.of(invoice.getYear(), invoice.getMonth(), day);
    }
}
