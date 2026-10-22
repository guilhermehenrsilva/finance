package com.finance.api.domain.account;

import com.finance.api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findByUser(User user);
    List<Account> findByUserAndActiveTrue(User user);
    Optional<Account> findByIdAndUser(String id, User user);
}