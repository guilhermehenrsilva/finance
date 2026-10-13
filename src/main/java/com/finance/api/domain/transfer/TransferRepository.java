package com.finance.api.domain.transfer;

import com.finance.api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, String> {
    List<Transfer> findByUserOrderByDateDesc(User user);
}
