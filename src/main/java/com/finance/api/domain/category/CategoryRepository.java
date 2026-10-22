package com.finance.api.domain.category;

import com.finance.api.domain.transaction.TransactionType;
import com.finance.api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {
    List<Category> findByUser(User user);
    List<Category> findByUserAndActiveTrue(User user);
    List<Category> findByUserAndType(User user, TransactionType type);
    List<Category> findByUserAndTypeAndActiveTrue(User user, TransactionType type);
    Optional<Category> findByIdAndUser(String id, User user);
}