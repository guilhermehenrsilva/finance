package com.finance.api.domain.creditcard;

import com.finance.api.domain.account.Account;
import com.finance.api.domain.account.AccountRepository;
import com.finance.api.domain.creditcard.dto.InvoicePaymentRequestDTO;
import com.finance.api.domain.user.User;
import com.finance.api.infra.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    @Test
    void shouldPayInvoiceAndRestoreCardLimit() {
        User user = user("user-id");
        Account account = new Account();
        account.setId("account-id");
        account.setUser(user);
        account.setCurrentBalance(new BigDecimal("500.00"));

        CreditCard card = new CreditCard();
        card.setId("card-id");
        card.setUser(user);
        card.setCreditLimit(new BigDecimal("1000.00"));
        card.setAvailableLimit(new BigDecimal("900.00"));
        card.setDueDay(10);

        Invoice invoice = new Invoice();
        invoice.setId("invoice-id");
        invoice.setCreditCard(card);
        invoice.setTotalAmount(new BigDecimal("100.00"));
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setMonth(1);
        invoice.setYear(2026);
        invoice.setStatus(InvoiceStatus.CLOSED);

        when(invoiceRepository.findById("invoice-id")).thenReturn(Optional.of(invoice));
        when(accountRepository.findById("account-id")).thenReturn(Optional.of(account));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        invoiceService.pay(user, "invoice-id", new InvoicePaymentRequestDTO("account-id", null));

        assertEquals(new BigDecimal("400.00"), account.getCurrentBalance());
        assertEquals(new BigDecimal("100.00"), invoice.getPaidAmount());
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertEquals(new BigDecimal("1000.00"), card.getAvailableLimit());
    }

    @Test
    void shouldAcceptPartialInvoicePayment() {
        User user = user("user-id");
        Account account = account(user, "account-id", "500.00");
        CreditCard card = card(user);
        Invoice invoice = invoice(card);

        when(invoiceRepository.findById("invoice-id")).thenReturn(Optional.of(invoice));
        when(accountRepository.findById("account-id")).thenReturn(Optional.of(account));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        invoiceService.pay(user, "invoice-id", new InvoicePaymentRequestDTO("account-id", new BigDecimal("40.00")));

        assertEquals(new BigDecimal("40.00"), invoice.getPaidAmount());
        assertEquals(InvoiceStatus.CLOSED, invoice.getStatus());
        assertEquals(new BigDecimal("460.00"), account.getCurrentBalance());
    }

    @Test
    void shouldRejectPaymentAbovePendingAmount() {
        User user = user("user-id");
        Account account = account(user, "account-id", "500.00");
        Invoice invoice = invoice(card(user));

        when(invoiceRepository.findById("invoice-id")).thenReturn(Optional.of(invoice));
        when(accountRepository.findById("account-id")).thenReturn(Optional.of(account));

        assertThrows(BusinessException.class, () -> invoiceService.pay(
                user, "invoice-id", new InvoicePaymentRequestDTO("account-id", new BigDecimal("101.00"))));
    }

    @Test
    void shouldRejectPaymentWithInsufficientAccountBalance() {
        User user = user("user-id");
        Account account = account(user, "account-id", "50.00");
        Invoice invoice = invoice(card(user));

        when(invoiceRepository.findById("invoice-id")).thenReturn(Optional.of(invoice));
        when(accountRepository.findById("account-id")).thenReturn(Optional.of(account));

        assertThrows(BusinessException.class, () -> invoiceService.pay(
                user, "invoice-id", new InvoicePaymentRequestDTO("account-id", null)));
    }

    private Account account(User user, String id, String balance) {
        Account account = new Account();
        account.setId(id);
        account.setUser(user);
        account.setCurrentBalance(new BigDecimal(balance));
        return account;
    }

    private CreditCard card(User user) {
        CreditCard card = new CreditCard();
        card.setId("card-id");
        card.setUser(user);
        card.setCreditLimit(new BigDecimal("1000.00"));
        card.setAvailableLimit(new BigDecimal("900.00"));
        card.setDueDay(10);
        return card;
    }

    private Invoice invoice(CreditCard card) {
        Invoice invoice = new Invoice();
        invoice.setId("invoice-id");
        invoice.setCreditCard(card);
        invoice.setTotalAmount(new BigDecimal("100.00"));
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setMonth(1);
        invoice.setYear(2026);
        invoice.setStatus(InvoiceStatus.CLOSED);
        return invoice;
    }

    private User user(String id) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        return user;
    }
}
