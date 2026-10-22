package com.finance.api.domain.account;

import com.finance.api.domain.account.dto.AccountRequestDTO;
import com.finance.api.domain.account.dto.AccountResponseDTO;
import com.finance.api.domain.account.dto.AccountUpdateDTO;
import com.finance.api.domain.user.User;
import com.finance.api.infra.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    public AccountResponseDTO createAccount(User user, AccountRequestDTO dto) {
        Account account = new Account();
        account.setUser(user);
        account.setName(dto.name());
        account.setType(dto.type());
        account.setInitialBalance(dto.initialBalance());
        account.setColor(dto.color());

        Account savedAccount = accountRepository.save(account);
        return new AccountResponseDTO(savedAccount);
    }

    public List<AccountResponseDTO> findUserAccounts(User user, Boolean activeOnly) {
        List<Account> accounts = (activeOnly != null && activeOnly)
                ? accountRepository.findByUserAndActiveTrue(user)
                : accountRepository.findByUser(user);

        return accounts.stream()
                .map(AccountResponseDTO::new)
                .collect(Collectors.toList());
    }

    public AccountResponseDTO getAccountById(User user, String id) {
        Account account = findAccountEntity(user, id);
        return new AccountResponseDTO(account);
    }

    @Transactional
    public AccountResponseDTO updateAccount(User user, String id, AccountUpdateDTO dto) {
        Account account = findAccountEntity(user, id);

        account.setName(dto.name());
        account.setType(dto.type());
        if (dto.color() != null) {
            account.setColor(dto.color());
        }
        if (dto.active() != null) {
            account.setActive(dto.active());
        }
        if (dto.initialBalance() != null) {
            BigDecimal diff = dto.initialBalance().subtract(account.getInitialBalance());
            account.setInitialBalance(dto.initialBalance());
            account.setCurrentBalance(account.getCurrentBalance().add(diff));
        }

        Account updated = accountRepository.save(account);
        return new AccountResponseDTO(updated);
    }

    @Transactional
    public void deleteAccount(User user, String id) {
        Account account = findAccountEntity(user, id);
        account.setActive(false);
        accountRepository.save(account);
    }

    public Account findAccountEntity(User user, String id) {
        return accountRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));
    }
}