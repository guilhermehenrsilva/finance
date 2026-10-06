package com.finance.api.domain.transaction;

import com.finance.api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByUserOrderByDateDesc(User user);
}