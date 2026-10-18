package com.covenantcode.crm.controller;

import com.covenantcode.crm.dto.analytics.DashboardResponse;
import com.covenantcode.crm.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<DashboardResponse> getDashboard(){
        DashboardResponse dashboard = analyticsService.getDashboard();
        return ResponseEntity.ok(dashboard);
    }
}
