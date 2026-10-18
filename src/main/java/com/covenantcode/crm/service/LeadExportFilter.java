package com.covenantcode.crm.service;

import com.covenantcode.crm.entity.enums.LeadStatus;

import java.time.LocalDate;

public record LeadExportFilter(
        LeadStatus status,
        String search,
        LocalDate dateFrom,
        LocalDate dateTo
) {
}
