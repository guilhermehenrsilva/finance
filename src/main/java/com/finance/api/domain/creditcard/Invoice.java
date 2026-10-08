package com.finance.api.domain.creditcard;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "invoices")
@Entity(name = "Invoice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_card_id", nullable = false)
    private CreditCard creditCard;

    private Integer month;

    private Integer year;

    @Column(name = "total_amount")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount")
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.totalAmount == null) this.totalAmount = BigDecimal.ZERO;
        if (this.paidAmount == null) this.paidAmount = BigDecimal.ZERO;
        if (this.status == null) this.status = InvoiceStatus.OPEN;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}