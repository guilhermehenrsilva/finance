package com.finance.api.domain.account;

import com.finance.api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findByUser(User user);
}