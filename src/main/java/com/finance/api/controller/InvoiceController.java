package com.finance.api.controller;

import com.finance.api.domain.creditcard.InvoiceService;
import com.finance.api.domain.creditcard.dto.InvoiceResponseDTO;
import com.finance.api.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping
    public List<InvoiceResponseDTO> list(@AuthenticationPrincipal User user) {
        return invoiceService.listUserInvoices(user);
    }
}
