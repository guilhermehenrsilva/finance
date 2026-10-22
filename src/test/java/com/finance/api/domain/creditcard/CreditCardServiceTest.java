package com.finance.api.domain.creditcard;

import com.finance.api.domain.account.AccountRepository;
import com.finance.api.domain.creditcard.dto.CreditCardRequestDTO;
import com.finance.api.domain.creditcard.dto.CreditCardResponseDTO;
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
class CreditCardServiceTest {

    @Mock
    private CreditCardRepository creditCardRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private CreditCardService creditCardService;

    @Test
    void shouldCreateCreditCard() {
        User user = new User("User", "user@test.com", "pass", "BRL");
        when(creditCardRepository.save(any(CreditCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreditCardRequestDTO dto = new CreditCardRequestDTO("Nubank Ultravioleta", null, new BigDecimal("5000.00"), 10, 17, "#8A05BE");
        CreditCardResponseDTO response = creditCardService.createCreditCard(user, dto);

        assertEquals("Nubank Ultravioleta", response.name());
        assertEquals(new BigDecimal("5000.00"), response.creditLimit());
        assertEquals(10, response.closingDay());
        assertEquals(17, response.dueDay());
    }

    @Test
    void shouldUpdateCreditCardLimitAndAdjustAvailable() {
        User user = new User("User", "user@test.com", "pass", "BRL");
        CreditCard card = new CreditCard();
        card.setUser(user);
        card.setName("Card");
        card.setCreditLimit(new BigDecimal("3000.00"));
        card.setAvailableLimit(new BigDecimal("2000.00"));
        card.setClosingDay(5);
        card.setDueDay(12);

        when(creditCardRepository.findByIdAndUser("card-1", user)).thenReturn(Optional.of(card));
        when(creditCardRepository.save(any(CreditCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreditCardRequestDTO dto = new CreditCardRequestDTO("Card Gold", null, new BigDecimal("4000.00"), 5, 12, "#FFD700");
        CreditCardResponseDTO response = creditCardService.updateCreditCard(user, "card-1", dto);

        assertEquals("Card Gold", response.name());
        assertEquals(new BigDecimal("4000.00"), response.creditLimit());
        assertEquals(new BigDecimal("3000.00"), response.availableLimit());
    }

    @Test
    void shouldSoftDeleteCreditCard() {
        User user = new User("User", "user@test.com", "pass", "BRL");
        CreditCard card = new CreditCard();
        card.setUser(user);
        card.setActive(true);

        when(creditCardRepository.findByIdAndUser("card-1", user)).thenReturn(Optional.of(card));
        when(creditCardRepository.save(any(CreditCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        creditCardService.deleteCreditCard(user, "card-1");

        assertFalse(card.getActive());
        verify(creditCardRepository).save(card);
    }
}
