package com.finance.api.domain.creditcard;

public enum InvoiceStatus {
    OPEN,    // Fatura do mês atual (aceitando novas compras)
    CLOSED,  // Fatura fechada (aguardando pagamento)
    PAID     // Fatura totalmente paga
}