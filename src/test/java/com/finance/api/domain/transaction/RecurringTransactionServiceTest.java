package com.finance.api.domain.transaction;

import com.finance.api.domain.account.AccountRepository;
import com.finance.api.domain.category.CategoryRepository;
import com.finance.api.domain.creditcard.CreditCardRepository;
import com.finance.api.domain.transaction.dto.RecurringTransactionRequestDTO;
import com.finance.api.domain.user.User;
import com.finance.api.infra.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RecurringTransactionServiceTest {

    @Mock
    private RecurringTransactionRepository recurringRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CreditCardRepository creditCardRepository;

    @InjectMocks
    private RecurringTransactionService recurringService;

    @Test
    void shouldRejectTransfers() {
        RecurringTransactionRequestDTO request = request(TransactionType.TRANSFER, null, "account-id");

        assertThrows(BusinessException.class, () -> recurringService.create(user(), request));
    }

    @Test
    void shouldRejectEndDateBeforeStartDate() {
        RecurringTransactionRequestDTO request = new RecurringTransactionRequestDTO(
                "Aluguel",
                new BigDecimal("1200.00"),
                TransactionType.EXPENSE,
                RecurringFrequency.MONTHLY,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 1, 31),
                "category-id",
                "account-id",
                null
        );

        assertThrows(BusinessException.class, () -> recurringService.create(user(), request));
    }

    private RecurringTransactionRequestDTO request(TransactionType type, String accountId, String creditCardId) {
        return new RecurringTransactionRequestDTO(
                "Aluguel",
                new BigDecimal("1200.00"),
                type,
                RecurringFrequency.MONTHLY,
                LocalDate.of(2026, 1, 1),
                null,
                "category-id",
                accountId,
                creditCardId
        );
    }

    private User user() {
        return mock(User.class);
    }
}
