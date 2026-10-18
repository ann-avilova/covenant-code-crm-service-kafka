package com.covenantcode.crm.dto.course;

import com.covenantcode.crm.entity.enums.CourseStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseUpdateRequest {

    @NotBlank(message = "Название курса не может быть пустым")
    @Size(max = 255, message = "Название курса не должно превышать 255 символов")
    private String title;

    private String description;

    @NotNull(message = "Продолжительность курса обязательна")
    @Positive(message = "Продолжительность курса должна быть положительным числом")
    private Integer durationInWeeks;

    @NotNull(message = "Стоимость курса обязательна")
    @PositiveOrZero(message = "Стоимость курса не может быть отрицательной")
    private BigDecimal price;

    @NotNull(message = "Статус курса обязателен")
    private CourseStatus status;
}
