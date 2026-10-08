package com.finance.api.controller;

import com.finance.api.domain.transaction.DashboardService;
import com.finance.api.domain.transaction.dto.DashboardSummaryDTO;
import com.finance.api.domain.user.User;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> summary(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        LocalDate resolvedStartDate = startDate != null
                ? startDate
                : DashboardService.defaultStartDate();
        LocalDate resolvedEndDate = endDate != null
                ? endDate
                : DashboardService.defaultEndDate();

        return ResponseEntity.ok(
                dashboardService.getSummary(user, resolvedStartDate, resolvedEndDate)
        );
    }
}
