package com.covenantcode.crm.service;

import com.covenantcode.crm.dto.attendance.AttendanceMarkRequest;
import com.covenantcode.crm.dto.attendance.AttendanceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AttendanceService {

    List<AttendanceResponse> markAttendance(Long lessonId, AttendanceMarkRequest request);

    List<AttendanceResponse> getAttendanceByLesson(Long lessonId);

    Page<AttendanceResponse> getAttendanceByStudent(Long studentId, Pageable pageable);
}
