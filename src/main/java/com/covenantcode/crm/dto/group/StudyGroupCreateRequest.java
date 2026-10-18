package com.covenantcode.crm.dto.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGroupCreateRequest {

    @NotBlank(message = "Название группы обязательно")
    @Size(max = 255, message = "Название группы не должно превышать 255 символов")
    private String name;

    @NotNull(message = "ID курса обязателен")
    private Long courseId;

    @NotNull(message = "ID учителя обязателен")
    private Long teacherId;

    @NotNull(message = "Дата начала обязательна")
    private LocalDate startDate;

    private Set<Long> studentIds;
}
