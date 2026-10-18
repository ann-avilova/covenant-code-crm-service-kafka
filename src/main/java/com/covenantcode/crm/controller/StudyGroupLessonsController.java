package com.covenantcode.crm.controller;

import com.covenantcode.crm.dto.lesson.LessonResponse;
import com.covenantcode.crm.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
public class StudyGroupLessonsController {

    private final LessonService lessonService;

    @GetMapping("/{groupId}/lessons")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEACHER', 'STUDENT')")
    @Operation(
            summary = "Получить расписание занятий группы",
            description = "Возвращает полный список занятий для указанной группы, отсортированный по дате и времени. " +
                    "ADMIN и MANAGER видят любую группу, TEACHER только свои группы, STUDENT только свои группы."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список занятий успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (недостаточно прав)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Группа не найдена", content = @Content)
    })
    public ResponseEntity<List<LessonResponse>> getLessonsByGroup(
            @Parameter(description = "Идентификатор учебной группы", required = true)
            @PathVariable Long groupId,
            Authentication authentication
    ) {
        List<LessonResponse> lessons = lessonService.getLessonsByGroup(groupId, authentication);
        return ResponseEntity.ok(lessons);
    }
}
