package com.finance.api.domain.account;

import com.finance.api.domain.account.dto.AccountRequestDTO;
import com.finance.api.domain.account.dto.AccountResponseDTO;
import com.finance.api.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

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

    public List<AccountResponseDTO> findUserAccounts(User user) {
        return accountRepository.findByUser(user).stream()
                .map(AccountResponseDTO::new)
                .collect(Collectors.toList());
    }
}