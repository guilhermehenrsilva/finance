package com.finance.api.domain.goal;

import com.finance.api.domain.goal.dto.*;
import com.finance.api.domain.user.User;
import com.finance.api.infra.exception.BusinessException;
import com.finance.api.infra.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FinancialGoalService {
    private final FinancialGoalRepository repository;

    public FinancialGoalService(FinancialGoalRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FinancialGoalResponseDTO create(User user, FinancialGoalRequestDTO dto) {
        FinancialGoal goal = new FinancialGoal();
        goal.setUser(user);
        goal.setName(dto.name().trim());
        goal.setTargetAmount(dto.targetAmount());
        goal.setDeadline(dto.deadline());
        return response(repository.save(goal));
    }

    @Transactional(readOnly = true)
    public List<FinancialGoalResponseDTO> list(User user) {
        return repository.findByUserOrderByDeadlineAscCreatedAtDesc(user).stream().map(this::response).toList();
    }

    @Transactional
    public FinancialGoalResponseDTO contribute(User user, String id, GoalContributionRequestDTO dto) {
        FinancialGoal goal = find(user, id);
        goal.setCurrentAmount(goal.getCurrentAmount().add(dto.amount()));
        return response(repository.save(goal));
    }

    @Transactional
    public void delete(User user, String id) {
        repository.delete(find(user, id));
    }

    private FinancialGoal find(User user, String id) {
        return repository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Meta financeira não encontrada"));
    }

    private FinancialGoalResponseDTO response(FinancialGoal goal) {
        return new FinancialGoalResponseDTO(goal);
    }
}
