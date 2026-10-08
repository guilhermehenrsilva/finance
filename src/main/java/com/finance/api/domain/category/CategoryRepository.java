package com.finance.api.domain.category;

import com.finance.api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, String> {
    List<Category> findByUser(User user);
}