package com.covenantcode.crm.controller;

import com.covenantcode.crm.dto.attendance.AttendanceMarkRequest;
import com.covenantcode.crm.dto.attendance.AttendanceResponse;
import com.covenantcode.crm.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/lessons/{lessonId}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEACHER')")
    public ResponseEntity<List<AttendanceResponse>> markAttendance(
            @PathVariable Long lessonId,
            @Valid @RequestBody AttendanceMarkRequest request) {
        List<AttendanceResponse> responses = attendanceService.markAttendance(lessonId, request);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/lessons/{lessonId}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEACHER')")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByLesson(
            @PathVariable Long lessonId) {
        List<AttendanceResponse> responses = attendanceService.getAttendanceByLesson(lessonId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/students/{studentId}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STUDENT')")
    public ResponseEntity<Page<AttendanceResponse>> getAttendanceByStudent(
            @PathVariable Long studentId,
            @PageableDefault(size = 20, sort = "markedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AttendanceResponse> page = attendanceService.getAttendanceByStudent(studentId, pageable);
        return ResponseEntity.ok(page);
    }
}
