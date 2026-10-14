package com.finance.api.domain.transaction;

import com.finance.api.domain.account.Account;
import com.finance.api.domain.account.AccountRepository;
import com.finance.api.domain.category.CategoryRepository;
import com.finance.api.domain.creditcard.CreditCardRepository;
import com.finance.api.domain.creditcard.InvoiceRepository;
import com.finance.api.domain.transaction.dto.TransactionRequestDTO;
import com.finance.api.domain.user.User;
import com.finance.api.infra.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import java.util.ArrayList;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CreditCardRepository creditCardRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldRejectTransferInGenericTransactionEndpoint() {
        User user = mock(User.class);
        TransactionRequestDTO request = new TransactionRequestDTO(
                "Transferência",
                new BigDecimal("100.00"),
                TransactionType.TRANSFER,
                TransactionStatus.PAID,
                LocalDate.of(2026, 1, 10),
                null,
                "account-id",
                null,
                1
        );

        assertThrows(
                BusinessException.class,
                () -> transactionService.createTransaction(user, request)
        );
    }

    @Test
    void shouldSubtractPaidExpenseFromAccountBalance() {
        User user = mock(User.class);
        when(user.getId()).thenReturn("user-id");
        Account account = new Account();
        account.setId("account-id");
        account.setUser(user);
        account.setCurrentBalance(new BigDecimal("500.00"));

        var category = new com.finance.api.domain.category.Category();
        category.setId("category-id");
        category.setUser(user);

        TransactionRequestDTO request = new TransactionRequestDTO(
                "Aluguel",
                new BigDecimal("120.00"),
                TransactionType.EXPENSE,
                TransactionStatus.PAID,
                LocalDate.of(2026, 1, 10),
                "category-id",
                "account-id",
                null,
                1
        );

        when(categoryRepository.findById("category-id")).thenReturn(java.util.Optional.of(category));
        when(accountRepository.findById("account-id")).thenReturn(java.util.Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        transactionService.createTransaction(user, request);

        assertEquals(new BigDecimal("380.00"), account.getCurrentBalance());
        verify(accountRepository).save(account);
    }

    @Test
    void shouldKeepInstallmentSumEqualToOriginalAmount() {
        User user = mock(User.class);
        when(user.getId()).thenReturn("user-id");
        var category = new com.finance.api.domain.category.Category();
        category.setId("category-id");
        category.setUser(user);

        var card = new com.finance.api.domain.creditcard.CreditCard();
        card.setId("card-id");
        card.setUser(user);
        card.setClosingDay(10);
        card.setAvailableLimit(new BigDecimal("1000.00"));

        when(categoryRepository.findById("category-id")).thenReturn(java.util.Optional.of(category));
        when(creditCardRepository.findById("card-id")).thenReturn(java.util.Optional.of(card));
        when(invoiceRepository.findByCreditCardAndMonthAndYear(any(), any(Integer.class), any(Integer.class)))
                .thenAnswer(invocation -> {
                    var invoice = new com.finance.api.domain.creditcard.Invoice();
                    invoice.setCreditCard(card);
                    invoice.setTotalAmount(BigDecimal.ZERO);
                    invoice.setPaidAmount(BigDecimal.ZERO);
                    return java.util.Optional.of(invoice);
                });
        TransactionRequestDTO request = new TransactionRequestDTO(
                "Compra parcelada",
                new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                TransactionStatus.PAID,
                LocalDate.of(2026, 1, 5),
                "category-id",
                null,
                "card-id",
                3
        );

        var saved = new ArrayList<Transaction>();
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            saved.add(transaction);
            return transaction;
        });

        transactionService.createTransaction(user, request);

        assertEquals(new BigDecimal("33.33"), saved.get(0).getAmount());
        assertEquals(new BigDecimal("33.33"), saved.get(1).getAmount());
        assertEquals(new BigDecimal("33.34"), saved.get(2).getAmount());
        assertEquals(new BigDecimal("100.00"), saved.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    @Test
    void shouldRejectAccountAndCreditCardTogether() {
        User user = mock(User.class);
        TransactionRequestDTO request = new TransactionRequestDTO(
                "Compra inválida",
                new BigDecimal("50.00"),
                TransactionType.EXPENSE,
                TransactionStatus.PAID,
                LocalDate.of(2026, 1, 10),
                "category-id",
                "account-id",
                "card-id",
                1
        );

        assertThrows(BusinessException.class, () -> transactionService.createTransaction(user, request));
    }
}
