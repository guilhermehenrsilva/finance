package com.finance.api.domain.creditcard;

import com.finance.api.domain.account.Account;
import com.finance.api.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "credit_cards")
@Entity(name = "CreditCard")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Conta padrão de onde o dinheiro sairá para pagar a fatura (opcional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    private String name;

    @Column(name = "credit_limit")
    private BigDecimal creditLimit;

    @Column(name = "available_limit")
    private BigDecimal availableLimit;

    @Column(name = "closing_day")
    private Integer closingDay;

    @Column(name = "due_day")
    private Integer dueDay;

    private String color;

    private Boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.availableLimit == null) {
            this.availableLimit = this.creditLimit != null ? this.creditLimit : BigDecimal.ZERO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}