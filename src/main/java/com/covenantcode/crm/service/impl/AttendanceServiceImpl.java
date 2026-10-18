package com.covenantcode.crm.service.impl;

import com.covenantcode.crm.dto.attendance.AttendanceMarkRequest;
import com.covenantcode.crm.dto.attendance.AttendanceRecord;
import com.covenantcode.crm.dto.attendance.AttendanceResponse;
import com.covenantcode.crm.entity.Attendance;
import com.covenantcode.crm.entity.Lesson;
import com.covenantcode.crm.entity.Student;
import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.enums.RoleName;
import com.covenantcode.crm.exception.BadRequestException;
import com.covenantcode.crm.exception.ResourceNotFoundException;
import com.covenantcode.crm.mapper.AttendanceMapper;
import com.covenantcode.crm.repository.AttendanceRepository;
import com.covenantcode.crm.repository.LessonRepository;
import com.covenantcode.crm.repository.StudentRepository;
import com.covenantcode.crm.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final LessonRepository lessonRepository;
    private final StudentRepository studentRepository;
    private final AttendanceMapper attendanceMapper;

    @Override
    @Transactional
    public List<AttendanceResponse> markAttendance(Long lessonId, AttendanceMarkRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));

        User currentUser = getCurrentUser();
        if (currentUser.getRole().getName() == RoleName.TEACHER
                && !lesson.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Преподаватель может отмечать только свои занятия");
        }

        if (lesson.getLessonDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Нельзя отметить посещаемость для будущего занятия");
        }

        if (request.getRecords() == null || request.getRecords().isEmpty()) {
            return List.of();
        }

        List<Attendance> toSave = new ArrayList<>();
        for (AttendanceRecord record : request.getRecords()) {
            Student student = studentRepository.findById(record.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student", record.getStudentId()));

            if (!lesson.getStudyGroup().getStudents().contains(student)) {
                throw new BadRequestException("Студент " + record.getStudentId() +
                        " не является участником группы занятия");
            }

            Optional<Attendance> existing = attendanceRepository
                    .findByLessonIdAndStudentId(lessonId, student.getId());

            Attendance attendance;
            if (existing.isPresent()) {
                attendance = existing.get();
                attendanceMapper.updateAttendanceFromRecord(record, attendance);
            } else {
                attendance = Attendance.builder()
                        .lesson(lesson)
                        .student(student)
                        .present(record.isPresent())
                        .note(record.getNote())
                        .build();
            }
            toSave.add(attendance);
        }

        List<Attendance> saved = attendanceRepository.saveAll(toSave);
        return saved.stream()
                .map(attendanceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceByLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));

        User currentUser = getCurrentUser();
        if (currentUser.getRole().getName() == RoleName.TEACHER
        && !lesson.getTeacher().getId().equals(currentUser.getId())){
            throw new AccessDeniedException("Преподаватель может просматривать только свои занятия");
        }

        List<Attendance> attendances = attendanceRepository.findAllByLessonId(lessonId);
        return attendances.stream()
                .map(attendanceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getAttendanceByStudent(Long studentId, Pageable pageable) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole().getName() == RoleName.STUDENT) {
            Student currentStudent = studentRepository.findByUser_Id(currentUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Студент не найден для текущего пользователя"));
            if (!currentStudent.getId().equals(studentId)) {
                throw new AccessDeniedException("Студент может просматривать только свою историю");
            }
        }

        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student", studentId);
        }

        Page<Attendance> page = attendanceRepository.findAllByStudentId(studentId, pageable);
        return page.map(attendanceMapper::toResponse);
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        throw new AccessDeniedException("Пользователь не аутентифицирован");
    }
}
