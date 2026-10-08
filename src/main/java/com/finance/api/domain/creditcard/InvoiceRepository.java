package com.finance.api.domain.creditcard;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    List<Invoice> findByCreditCard(CreditCard creditCard);
    
    // Método crucial para descobrir qual fatura abater na hora de uma nova compra
    Optional<Invoice> findByCreditCardAndMonthAndYear(CreditCard creditCard, Integer month, Integer year);
}