package com.finance.api.domain.transaction;

import com.finance.api.domain.account.AccountRepository;
import com.finance.api.domain.transaction.dto.CategoryDashboardDTO;
import com.finance.api.domain.transaction.dto.DashboardSummaryDTO;
import com.finance.api.domain.transaction.dto.TransactionResponseDTO;
import com.finance.api.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public DashboardService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary(User user, LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);

        List<Transaction> transactions = transactionRepository
                .findByUserAndDateBetweenOrderByDateDesc(user, startDate, endDate);

        BigDecimal totalBalance = accountRepository.findByUser(user).stream()
                .filter(account -> Boolean.TRUE.equals(account.getActive()))
                .map(account -> account.getCurrentBalance() == null
                        ? BigDecimal.ZERO
                        : account.getCurrentBalance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal periodIncome = sumByTypeAndStatus(transactions, TransactionType.INCOME, TransactionStatus.PAID);
        BigDecimal periodExpense = sumByTypeAndStatus(transactions, TransactionType.EXPENSE, TransactionStatus.PAID);
        BigDecimal pendingIncome = sumByTypeAndStatus(transactions, TransactionType.INCOME, TransactionStatus.PENDING);
        BigDecimal pendingExpense = sumByTypeAndStatus(transactions, TransactionType.EXPENSE, TransactionStatus.PENDING);

        Map<String, BigDecimal> expensesByCategory = new LinkedHashMap<>();
        transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .filter(transaction -> transaction.getStatus() == TransactionStatus.PAID)
                .forEach(transaction -> {
                    String categoryName = transaction.getCategory() == null
                            ? "Sem categoria"
                            : transaction.getCategory().getName();
                    expensesByCategory.merge(categoryName, transaction.getAmount(), BigDecimal::add);
                });

        List<CategoryDashboardDTO> categorySummary = expensesByCategory.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue(Comparator.reverseOrder()))
                .map(entry -> new CategoryDashboardDTO(entry.getKey(), entry.getValue()))
                .toList();

        List<TransactionResponseDTO> recentTransactions = transactions.stream()
                .limit(10)
                .map(TransactionResponseDTO::new)
                .toList();

        return new DashboardSummaryDTO(
                startDate,
                endDate,
                totalBalance,
                periodIncome,
                periodExpense,
                periodIncome.subtract(periodExpense),
                pendingIncome,
                pendingExpense,
                categorySummary,
                recentTransactions
        );
    }

    private BigDecimal sumByTypeAndStatus(
            List<Transaction> transactions,
            TransactionType type,
            TransactionStatus status
    ) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == type)
                .filter(transaction -> transaction.getStatus() == status)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("O período informado é inválido");
        }
    }

    public static LocalDate defaultStartDate() {
        return YearMonth.now().atDay(1);
    }

    public static LocalDate defaultEndDate() {
        return YearMonth.now().atEndOfMonth();
    }
}
