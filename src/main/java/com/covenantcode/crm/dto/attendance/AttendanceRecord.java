package com.covenantcode.crm.dto.attendance;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord {

    @NotNull(message = "ID студента обязателен")
    private Long studentId;

    @NotNull(message = "Признак присутствия обязателен")
    private boolean present;

    @Size(max = 255, message = "Комментарий не может превышать 255 символов")
    private String note;
}
