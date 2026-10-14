package com.finance.api.domain.transaction;

import com.finance.api.domain.category.Category;
import com.finance.api.domain.transaction.dto.TransactionRequestDTO;
import com.finance.api.domain.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecurringTransactionProcessingTest {

    @Mock
    private RecurringTransactionRepository recurringRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private RecurringTransactionService recurringService;

    @Test
    void shouldCreateAllMissedMonthlyTransactions() {
        RecurringTransaction recurring = recurring(
                RecurringFrequency.MONTHLY,
                LocalDate.of(2026, 1, 10),
                null
        );
        when(recurringRepository.findAllByActiveTrueAndNextDateLessThanEqual(LocalDate.of(2026, 3, 10)))
                .thenReturn(List.of(recurring));

        recurringService.processRecurringTransactions(LocalDate.of(2026, 3, 10));

        ArgumentCaptor<TransactionRequestDTO> captor = ArgumentCaptor.forClass(TransactionRequestDTO.class);
        verify(transactionService, org.mockito.Mockito.times(3))
                .createTransaction(any(User.class), captor.capture());
        assertEquals(List.of(
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 2, 10),
                LocalDate.of(2026, 3, 10)
        ), captor.getAllValues().stream().map(TransactionRequestDTO::date).toList());
        assertEquals(LocalDate.of(2026, 4, 10), recurring.getNextDate());
    }

    @Test
    void shouldDeactivateAfterProcessingLastEndDate() {
        RecurringTransaction recurring = recurring(
                RecurringFrequency.WEEKLY,
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 19)
        );
        when(recurringRepository.findAllByActiveTrueAndNextDateLessThanEqual(LocalDate.of(2026, 1, 31)))
                .thenReturn(List.of(recurring));

        recurringService.processRecurringTransactions(LocalDate.of(2026, 1, 31));

        verify(transactionService, org.mockito.Mockito.times(3))
                .createTransaction(any(User.class), any(TransactionRequestDTO.class));
        assertEquals(LocalDate.of(2026, 1, 26), recurring.getNextDate());
        assertEquals(false, recurring.getActive());
        verify(recurringRepository).save(recurring);
    }

    private RecurringTransaction recurring(
            RecurringFrequency frequency,
            LocalDate nextDate,
            LocalDate endDate
    ) {
        User user = org.mockito.Mockito.mock(User.class);
        Category category = new Category();
        category.setId("category-id");

        RecurringTransaction recurring = new RecurringTransaction();
        recurring.setUser(user);
        recurring.setCategory(category);
        recurring.setDescription("Assinatura");
        recurring.setAmount(new BigDecimal("50.00"));
        recurring.setType(TransactionType.EXPENSE);
        recurring.setFrequency(frequency);
        recurring.setNextDate(nextDate);
        recurring.setEndDate(endDate);
        recurring.setActive(true);
        recurring.setAccount(null);
        recurring.setCreditCard(null);
        return recurring;
    }
}
