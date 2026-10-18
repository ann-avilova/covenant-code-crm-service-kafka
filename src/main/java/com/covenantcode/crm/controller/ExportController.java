package com.covenantcode.crm.controller;

import com.covenantcode.crm.entity.enums.LeadStatus;
import com.covenantcode.crm.service.ExportService;
import com.covenantcode.crm.service.LeadExportFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/export")
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/leads")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Экспорт лидов в CSV",
            description = "Экспортирует список лидов в CSV-файл с возможностью фильтрации по статусу, текстовому поиску и диапазону дат"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "CSV-файл успешно создан и отправлен",
                    content = @Content(mediaType = "text/csv;charset=UTF-8")
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется ADMIN или MANAGER)"),
    })
    public void exportLeads(
            @RequestParam(required = false) LeadStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            HttpServletResponse response) {

        try {
            String filename = "leads_" + LocalDate.now() + ".csv";
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            exportService.exportLeads(new LeadExportFilter(status, search, dateFrom, dateTo),
                    response.getOutputStream());
        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка при экспорте лидов", e);
        }
    }

    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Экспорт студентов в CSV",
            description = "Экспортирует список студентов в CSV-файл с возможностью фильтрации по текстовому поиску"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "CSV-файл успешно создан и отправлен",
                    content = @Content(mediaType = "text/csv;charset=UTF-8")
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется ADMIN или MANAGER)")
    })
    public void exportStudents(
            @RequestParam(required = false) String search,
            HttpServletResponse response) {

        try {
            String filename = "students_" + LocalDate.now() + ".csv";
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            exportService.exportStudents(search, response.getOutputStream());
        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка при экспорте студентов", e);
        }
    }
}
