package com.finance.api.domain.goal;

import com.finance.api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, String> {
    List<FinancialGoal> findByUserOrderByDeadlineAscCreatedAtDesc(User user);
    Optional<FinancialGoal> findByIdAndUser(String id, User user);
}
