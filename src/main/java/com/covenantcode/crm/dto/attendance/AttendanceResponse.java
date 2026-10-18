package com.covenantcode.crm.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private Long id;

    private Long lessonId;

    private LocalDate lessonDate;

    private Long studentId;

    private String studentName;

    private boolean present;

    private String note;

    private LocalDateTime markedAt;
}
