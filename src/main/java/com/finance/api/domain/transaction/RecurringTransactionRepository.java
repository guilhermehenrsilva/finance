package com.finance.api.domain.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, String> {
    
    // Busca todas as recorrências ativas cuja próxima data de cobrança chegou ou já passou
    List<RecurringTransaction> findAllByActiveTrueAndNextDateLessThanEqual(LocalDate date);
}