package com.finance.api.domain.creditcard;

import com.finance.api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CreditCardRepository extends JpaRepository<CreditCard, String> {
    List<CreditCard> findByUser(User user);
}