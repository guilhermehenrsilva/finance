package com.finance.api.domain.creditcard;

import com.finance.api.domain.account.Account;
import com.finance.api.domain.account.AccountRepository;
import com.finance.api.domain.creditcard.dto.CreditCardRequestDTO;
import com.finance.api.domain.creditcard.dto.CreditCardResponseDTO;
import com.finance.api.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreditCardService {

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private AccountRepository accountRepository;

    public CreditCardResponseDTO createCreditCard(User user, CreditCardRequestDTO dto) {
        CreditCard card = new CreditCard();
        card.setUser(user);
        card.setName(dto.name());
        card.setCreditLimit(dto.creditLimit());
        card.setClosingDay(dto.closingDay());
        card.setDueDay(dto.dueDay());
        card.setColor(dto.color());

        // Se o utilizador informou uma conta bancária para débito da fatura
        if (dto.accountId() != null && !dto.accountId().isBlank()) {
            Account account = accountRepository.findById(dto.accountId())
                    .filter(acc -> acc.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new RuntimeException("Conta não encontrada ou não pertence ao usuário"));
            card.setAccount(account);
        }

        CreditCard saved = creditCardRepository.save(card);
        return new CreditCardResponseDTO(saved);
    }

    public List<CreditCardResponseDTO> listUserCreditCards(User user) {
        return creditCardRepository.findByUser(user).stream()
                .map(CreditCardResponseDTO::new)
                .collect(Collectors.toList());
    }
}