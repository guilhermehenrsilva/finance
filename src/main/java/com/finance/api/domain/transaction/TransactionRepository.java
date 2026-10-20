package com.finance.api.domain.transaction;

import com.finance.api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;
import com.finance.api.domain.category.Category;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByUserOrderByDateDesc(User user);

    @org.springframework.data.jpa.repository.Query("""
            SELECT t FROM Transaction t
            WHERE t.user = :user
              AND (:startDate IS NULL OR t.date >= :startDate)
              AND (:endDate IS NULL OR t.date <= :endDate)
              AND (:type IS NULL OR t.type = :type)
              AND (:status IS NULL OR t.status = :status)
              AND (:categoryId IS NULL OR t.category.id = :categoryId)
              AND (:accountId IS NULL OR t.account.id = :accountId)
              AND (:creditCardId IS NULL OR t.creditCard.id = :creditCardId)
              AND (:description IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%')))
            ORDER BY t.date DESC
            """)
    List<Transaction> search(
            User user,
            LocalDate startDate,
            LocalDate endDate,
            TransactionType type,
            TransactionStatus status,
            String categoryId,
            String accountId,
            String creditCardId,
            String description
    );

    List<Transaction> findByUserAndDateBetweenOrderByDateDesc(
            User user,
            LocalDate startDate,
            LocalDate endDate
    );

    @org.springframework.data.jpa.repository.Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
            WHERE t.user = :user
              AND t.category = :category
              AND t.type = :type
              AND t.status = :status
              AND t.date BETWEEN :startDate AND :endDate
            """)
    BigDecimal sumPaidExpensesByUserAndCategoryAndDateBetween(
            User user, Category category, LocalDate startDate, LocalDate endDate,
            TransactionType type, TransactionStatus status
    );
}