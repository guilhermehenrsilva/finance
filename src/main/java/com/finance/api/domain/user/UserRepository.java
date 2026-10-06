package com.finance.api.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserRepository extends JpaRepository<User, String> {
    
    // O Spring Data JPA cria a query SQL automaticamente baseado no nome do método!
    UserDetails findByEmail(String email);
    
}