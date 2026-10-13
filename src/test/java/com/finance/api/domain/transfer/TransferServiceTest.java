package com.finance.api.domain.transfer;

import com.finance.api.domain.account.Account;
import com.finance.api.domain.account.AccountRepository;
import com.finance.api.domain.transfer.dto.TransferRequestDTO;
import com.finance.api.domain.user.User;
import com.finance.api.infra.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransferService transferService;

    @Test
    void shouldMoveBalanceBetweenAccounts() {
        User user = user("user-id");
        Account source = account("source", user, "500.00");
        Account destination = account("destination", user, "100.00");
        when(accountRepository.findById("source")).thenReturn(Optional.of(source));
        when(accountRepository.findById("destination")).thenReturn(Optional.of(destination));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        transferService.create(user, new TransferRequestDTO(
                "source", "destination", new BigDecimal("125.00"),
                LocalDate.of(2026, 1, 10), "Reserva"
        ));

        assertEquals(new BigDecimal("375.00"), source.getCurrentBalance());
        assertEquals(new BigDecimal("225.00"), destination.getCurrentBalance());
    }

    @Test
    void shouldRejectTransferWithInsufficientBalance() {
        User user = user("user-id");
        Account source = account("source", user, "50.00");
        Account destination = account("destination", user, "100.00");
        when(accountRepository.findById("source")).thenReturn(Optional.of(source));
        when(accountRepository.findById("destination")).thenReturn(Optional.of(destination));

        assertThrows(
                BusinessException.class,
                () -> transferService.create(user, new TransferRequestDTO(
                        "source", "destination", new BigDecimal("125.00"),
                        LocalDate.of(2026, 1, 10), null
                ))
        );
    }

    private User user(String id) {
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getId()).thenReturn(id);
        return user;
    }

    private Account account(String id, User user, String balance) {
        Account account = new Account();
        account.setId(id);
        account.setUser(user);
        account.setCurrentBalance(new BigDecimal(balance));
        return account;
    }
}
