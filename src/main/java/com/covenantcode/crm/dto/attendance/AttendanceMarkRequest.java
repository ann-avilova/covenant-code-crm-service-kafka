package com.covenantcode.crm.dto.attendance;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceMarkRequest {

    @NotEmpty(message = "Список отметок не должен быть пустым")
    private List<AttendanceRecord> records;
}
