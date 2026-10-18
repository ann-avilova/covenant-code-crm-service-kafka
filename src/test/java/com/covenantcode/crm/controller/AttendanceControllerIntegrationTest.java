package com.covenantcode.crm.controller;

import com.covenantcode.crm.BaseIntegrationTest;
import com.covenantcode.crm.dto.attendance.AttendanceMarkRequest;
import com.covenantcode.crm.dto.attendance.AttendanceRecord;
import com.covenantcode.crm.entity.Attendance;
import com.covenantcode.crm.entity.Course;
import com.covenantcode.crm.entity.Lesson;
import com.covenantcode.crm.entity.Role;
import com.covenantcode.crm.entity.Student;
import com.covenantcode.crm.entity.StudyGroup;
import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.enums.CourseStatus;
import com.covenantcode.crm.entity.enums.GroupStatus;
import com.covenantcode.crm.entity.enums.RoleName;
import com.covenantcode.crm.repository.AttendanceRepository;
import com.covenantcode.crm.repository.CourseRepository;
import com.covenantcode.crm.repository.LessonRepository;
import com.covenantcode.crm.repository.RoleRepository;
import com.covenantcode.crm.repository.StudentRepository;
import com.covenantcode.crm.repository.StudyGroupRepository;
import com.covenantcode.crm.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
public class AttendanceControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    private Lesson lesson;
    private Student student1;
    private Student student2;
    private StudyGroup group;

    @BeforeEach
    void setUp() {
        Role teacherRole = roleRepository.findByName(RoleName.TEACHER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.TEACHER).build()));
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ADMIN).build()));

        User admin = userRepository.save(
                User.builder()
                        .firstName("Admin")
                        .lastName("Test")
                        .email("admin@test.com")
                        .password("password")
                        .role(adminRole)
                        .enabled(true)
                        .build()
        );

        User teacher = userRepository.save(
                User.builder()
                        .firstName("Teacher")
                        .lastName("One")
                        .email("teacher@test.com")
                        .password("password")
                        .role(teacherRole)
                        .enabled(true)
                        .build()
        );

        Course course = courseRepository.save(
                Course.builder()
                        .title("Java Basics")
                        .description("Intro course")
                        .durationInWeeks(12)
                        .price(BigDecimal.valueOf(199.99))
                        .status(CourseStatus.ACTIVE)
                        .build()
        );

        group = studyGroupRepository.save(
                StudyGroup.builder()
                        .name("Group A")
                        .teacher(teacher)
                        .course(course)
                        .startDate(LocalDate.now())
                        .status(GroupStatus.ACTIVE)
                        .build()
        );

        student1 = studentRepository.save(
                Student.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .email("john@example.com")
                        .studyGroups(Set.of(group))
                        .build()
        );
        student2 = studentRepository.save(
                Student.builder()
                        .firstName("Jane")
                        .lastName("Smith")
                        .email("jane@example.com")
                        .studyGroups(Set.of(group))
                        .build()
        );

        Set<Student> students = new HashSet<>();
        students.add(student1);
        students.add(student2);
        group.setStudents(students);
        group = studyGroupRepository.save(group);

        lesson = lessonRepository.save(
                Lesson.builder()
                        .lessonDate(LocalDate.now())
                        .teacher(teacher)
                        .studyGroup(group)
                        .topic("Lesson 1")
                        .startTime(LocalTime.of(10, 0))
                        .endTime(LocalTime.of(11, 30))
                        .build()
        );
    }

    @Test
    @WithUserDetails(value = "admin@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void testMarkAttendance_Success() throws Exception {
        AttendanceRecord record1 = AttendanceRecord.builder()
                .studentId(student1.getId())
                .present(true)
                .note("Отлично")
                .build();
        AttendanceRecord record2 = AttendanceRecord.builder()
                .studentId(student2.getId())
                .present(false)
                .note("Болел")
                .build();
        AttendanceMarkRequest request = AttendanceMarkRequest.builder()
                .records(List.of(record1, record2))
                .build();

        mockMvc.perform(post("/api/v1/lessons/{lessonId}/attendance", lesson.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].studentId").value(student1.getId()))
                .andExpect(jsonPath("$[0].present").value(true))
                .andExpect(jsonPath("$[1].studentId").value(student2.getId()))
                .andExpect(jsonPath("$[1].present").value(false));

        List<Attendance> saved = attendanceRepository.findAllByLessonId(lesson.getId());
        assertThat(saved).hasSize(2);
        assertThat(saved).extracting(Attendance::getStudent).containsExactlyInAnyOrder(student1, student2);
        assertThat(saved).extracting(Attendance::isPresent).containsExactlyInAnyOrder(true, false);
    }

    @Test
    @WithUserDetails(value = "admin@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void testMarkAttendance_StudentNotInGroup_ShouldReturn400() throws Exception {
        Student outsider = studentRepository.save(
                Student.builder()
                        .firstName("Out")
                        .lastName("Sider")
                        .email("out@example.com")
                        .build()
        );

        AttendanceRecord record = AttendanceRecord.builder()
                .studentId(outsider.getId())
                .present(true)
                .build();
        AttendanceMarkRequest request = AttendanceMarkRequest.builder()
                .records(List.of(record))
                .build();

        mockMvc.perform(post("/api/v1/lessons/{lessonId}/attendance", lesson.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("не является участником группы занятия")));

        assertThat(attendanceRepository.findAllByLessonId(lesson.getId())).isEmpty();
    }

    @Test
    @WithUserDetails(value = "admin@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void testMarkAttendance_LessonNotFound_ShouldReturn404() throws Exception {
        Long nonExistentLessonId = 99999L;

        AttendanceRecord record = AttendanceRecord.builder()
                .studentId(student1.getId())
                .present(true)
                .build();
        AttendanceMarkRequest request = AttendanceMarkRequest.builder()
                .records(List.of(record))
                .build();

        mockMvc.perform(post("/api/v1/lessons/{lessonId}/attendance", nonExistentLessonId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(containsString("Lesson")));
    }

    @Test
    @WithUserDetails(value = "admin@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void testGetAttendanceByStudent_Success() throws Exception {
        Lesson lesson2 = lessonRepository.save(
                Lesson.builder()
                        .lessonDate(LocalDate.now().minusDays(1))
                        .teacher(lesson.getTeacher())
                        .studyGroup(group)
                        .topic("Lesson 2")
                        .startTime(LocalTime.of(12, 0))
                        .endTime(LocalTime.of(13, 30))
                        .build()
        );

        Attendance att1 = Attendance.builder()
                .lesson(lesson)
                .student(student1)
                .present(true)
                .note("Присутствовал")
                .build();
        Attendance att2 = Attendance.builder()
                .lesson(lesson2)
                .student(student1)
                .present(false)
                .note("Отсутствовал")
                .build();
        attendanceRepository.saveAll(List.of(att1, att2));

        mockMvc.perform(get("/api/v1/students/{studentId}/attendance", student1.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].studentId").value(student1.getId()))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }
}
