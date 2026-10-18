package com.covenantcode.crm.service.impl;

import com.covenantcode.crm.dto.attendance.AttendanceMarkRequest;
import com.covenantcode.crm.dto.attendance.AttendanceRecord;
import com.covenantcode.crm.dto.attendance.AttendanceResponse;
import com.covenantcode.crm.entity.Attendance;
import com.covenantcode.crm.entity.Lesson;
import com.covenantcode.crm.entity.Role;
import com.covenantcode.crm.entity.Student;
import com.covenantcode.crm.entity.StudyGroup;
import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.enums.RoleName;
import com.covenantcode.crm.exception.BadRequestException;
import com.covenantcode.crm.mapper.AttendanceMapper;
import com.covenantcode.crm.repository.AttendanceRepository;
import com.covenantcode.crm.repository.LessonRepository;
import com.covenantcode.crm.repository.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AttendanceServiceImplTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AttendanceMapper attendanceMapper;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private User teacherUser;
    private User adminUser;
    private Lesson lesson;
    private StudyGroup group;
    private Student student;
    private AttendanceRecord record;
    private AttendanceMarkRequest request;

    @BeforeEach
    void setUp() {
        Role teacherRole = Role.builder()
                .name(RoleName.TEACHER)
                .build();
        teacherUser = User.builder()
                .id(100L)
                .firstName("Teacher")
                .lastName("One")
                .role(teacherRole)
                .build();

        Role adminRole = Role.builder()
                .name(RoleName.ADMIN)
                .build();
        adminUser = User.builder()
                .id(999L)
                .firstName("Admin")
                .lastName("User")
                .role(adminRole)
                .build();

        student = Student.builder()
                .id(10L)
                .firstName("John")
                .lastName("Doe")
                .build();

        group = StudyGroup.builder()
                .id(1L)
                .students(Set.of(student))
                .build();

        lesson = Lesson.builder()
                .id(1L)
                .lessonDate(LocalDate.now())
                .teacher(teacherUser)
                .studyGroup(group)
                .build();

        record = AttendanceRecord.builder()
                .studentId(10L)
                .present(true)
                .note("Отлично")
                .build();

        request = AttendanceMarkRequest.builder()
                .records(List.of(record))
                .build();
    }

    @AfterEach
    void clearAuthentication(){
        SecurityContextHolder.clearContext();
    }

    private void setAuthentication(User user) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void markAttendance_shouldCreateNewRecords_whenNoExisting() {
        setAuthentication(teacherUser);

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(attendanceRepository.findByLessonIdAndStudentId(1L, 10L))
                .thenReturn(Optional.empty());

        when(attendanceRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(attendanceMapper.toResponse(any(Attendance.class)))
                .thenReturn(new AttendanceResponse());

        attendanceService.markAttendance(1L, request);

        verify(attendanceRepository, times(1)).saveAll(anyList());
    }

    @Test
    void markAttendance_shouldUpdateExistingRecord_whenRecordExists() {
        setAuthentication(teacherUser);

        Attendance existingAttendance = Attendance.builder()
                .id(1L)
                .lesson(lesson)
                .student(student)
                .present(false)
                .note("Старая заметка")
                .build();

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(attendanceRepository.findByLessonIdAndStudentId(1L, 10L))
                .thenReturn(Optional.of(existingAttendance));

        when(attendanceRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(attendanceMapper.toResponse(any(Attendance.class)))
                .thenReturn(new AttendanceResponse());

        attendanceService.markAttendance(1L, request);

//        assertThat(existingAttendance.isPresent()).isTrue();
//        assertThat(existingAttendance.getNote()).isEqualTo("Отлично");
//
//        verify(attendanceRepository, times(1)).saveAll(anyList());
        verify(attendanceMapper).updateAttendanceFromRecord(record, existingAttendance);
        verify(attendanceRepository, times(1)).saveAll(anyList());
    }

    @Test
    void markAttendance_shouldThrowBadRequest_whenStudentNotInGroup() {
        setAuthentication(teacherUser);

        group.setStudents(Set.of());

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> attendanceService.markAttendance(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("не является участником группы занятия");

        verify(attendanceRepository, never()).saveAll(anyList());
        verify(attendanceRepository, never()).findByLessonIdAndStudentId(anyLong(), anyLong());
    }
}
