package com.finance.api.domain.budget;

import com.finance.api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, String> {
    List<Budget> findByUserAndYearAndMonthOrderByCategory_NameAsc(User user, Integer year, Integer month);
    Optional<Budget> findByIdAndUser(String id, User user);
    boolean existsByUserAndCategoryIdAndMonthAndYear(User user, String categoryId, Integer month, Integer year);
}
