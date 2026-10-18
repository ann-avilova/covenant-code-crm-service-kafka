package com.covenantcode.crm.dto.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGroupUpdateRequest {
    @NotBlank(message = "Название группы")
    @Size(max = 255, message = "Название группы не должно превышать 255 символов")
    private String name;

    @NotNull(message = "ID курса")
    private Long courseId;

    @NotNull(message = "ID пользователя с ролью TEACHER")
    private Long teacherId;

    @NotNull(message = "Дата начала занятий")
    private LocalDate startDate;
}
