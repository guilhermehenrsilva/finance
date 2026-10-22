package com.finance.api.domain.account;

import com.finance.api.domain.account.dto.AccountRequestDTO;
import com.finance.api.domain.account.dto.AccountResponseDTO;
import com.finance.api.domain.account.dto.AccountUpdateDTO;
import com.finance.api.domain.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void shouldCreateAccount() {
        User user = new User("User", "user@test.com", "pass", "BRL");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountRequestDTO dto = new AccountRequestDTO("Nubank", "CORRENTE", new BigDecimal("1000.00"), "#8A05BE");
        AccountResponseDTO response = accountService.createAccount(user, dto);

        assertEquals("Nubank", response.name());
        assertEquals("CORRENTE", response.type());
        assertEquals(new BigDecimal("1000.00"), response.initialBalance());
    }

    @Test
    void shouldUpdateAccountAndAdjustBalance() {
        User user = new User("User", "user@test.com", "pass", "BRL");
        Account account = new Account();
        account.setUser(user);
        account.setName("Old Name");
        account.setType("CORRENTE");
        account.setInitialBalance(new BigDecimal("500.00"));
        account.setCurrentBalance(new BigDecimal("700.00"));

        when(accountRepository.findByIdAndUser("acc-1", user)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountUpdateDTO updateDTO = new AccountUpdateDTO("New Name", "POUPANCA", new BigDecimal("600.00"), "#00FF00", true);
        AccountResponseDTO response = accountService.updateAccount(user, "acc-1", updateDTO);

        assertEquals("New Name", response.name());
        assertEquals("POUPANCA", response.type());
        assertEquals(new BigDecimal("600.00"), response.initialBalance());
        assertEquals(new BigDecimal("800.00"), response.currentBalance());
    }

    @Test
    void shouldSoftDeleteAccount() {
        User user = new User("User", "user@test.com", "pass", "BRL");
        Account account = new Account();
        account.setUser(user);
        account.setActive(true);

        when(accountRepository.findByIdAndUser("acc-1", user)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        accountService.deleteAccount(user, "acc-1");

        assertFalse(account.getActive());
        verify(accountRepository).save(account);
    }
}
