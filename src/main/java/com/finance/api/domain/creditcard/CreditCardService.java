package com.finance.api.domain.creditcard;

import com.finance.api.domain.account.Account;
import com.finance.api.domain.account.AccountRepository;
import com.finance.api.domain.creditcard.dto.CreditCardRequestDTO;
import com.finance.api.domain.creditcard.dto.CreditCardResponseDTO;
import com.finance.api.domain.user.User;
import com.finance.api.infra.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreditCardService {

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    public CreditCardResponseDTO createCreditCard(User user, CreditCardRequestDTO dto) {
        CreditCard card = new CreditCard();
        card.setUser(user);
        card.setName(dto.name());
        card.setCreditLimit(dto.creditLimit());
        card.setClosingDay(dto.closingDay());
        card.setDueDay(dto.dueDay());
        card.setColor(dto.color());

        if (dto.accountId() != null && !dto.accountId().isBlank()) {
            Account account = accountRepository.findById(dto.accountId())
                    .filter(acc -> acc.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada ou não pertence ao usuário"));
            card.setAccount(account);
        }

        CreditCard saved = creditCardRepository.save(card);
        return new CreditCardResponseDTO(saved);
    }

    public List<CreditCardResponseDTO> listUserCreditCards(User user) {
        return listUserCreditCards(user, null);
    }

    public List<CreditCardResponseDTO> listUserCreditCards(User user, Boolean activeOnly) {
        List<CreditCard> cards = (activeOnly != null && activeOnly)
                ? creditCardRepository.findByUserAndActiveTrue(user)
                : creditCardRepository.findByUser(user);

        return cards.stream()
                .map(CreditCardResponseDTO::new)
                .collect(Collectors.toList());
    }

    public CreditCardResponseDTO getCreditCardById(User user, String id) {
        CreditCard card = findCardEntity(user, id);
        return new CreditCardResponseDTO(card);
    }

    @Transactional
    public CreditCardResponseDTO updateCreditCard(User user, String id, CreditCardRequestDTO dto) {
        CreditCard card = findCardEntity(user, id);

        card.setName(dto.name());
        card.setClosingDay(dto.closingDay());
        card.setDueDay(dto.dueDay());
        card.setColor(dto.color());

        if (dto.creditLimit() != null && !dto.creditLimit().equals(card.getCreditLimit())) {
            BigDecimal diff = dto.creditLimit().subtract(card.getCreditLimit());
            card.setCreditLimit(dto.creditLimit());
            card.setAvailableLimit(card.getAvailableLimit().add(diff));
        }

        if (dto.accountId() != null && !dto.accountId().isBlank()) {
            Account account = accountRepository.findById(dto.accountId())
                    .filter(acc -> acc.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada ou não pertence ao usuário"));
            card.setAccount(account);
        } else {
            card.setAccount(null);
        }

        CreditCard updated = creditCardRepository.save(card);
        return new CreditCardResponseDTO(updated);
    }

    @Transactional
    public void deleteCreditCard(User user, String id) {
        CreditCard card = findCardEntity(user, id);
        card.setActive(false);
        creditCardRepository.save(card);
    }

    public CreditCard findCardEntity(User user, String id) {
        return creditCardRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Cartão de crédito não encontrado"));
    }
}