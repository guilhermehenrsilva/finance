package com.finance.api.domain.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DashboardSummaryDTO(
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalBalance,
        BigDecimal periodIncome,
        BigDecimal periodExpense,
        BigDecimal periodResult,
        BigDecimal pendingIncome,
        BigDecimal pendingExpense,
        List<CategoryDashboardDTO> expensesByCategory,
        List<TransactionResponseDTO> recentTransactions
) {
}
