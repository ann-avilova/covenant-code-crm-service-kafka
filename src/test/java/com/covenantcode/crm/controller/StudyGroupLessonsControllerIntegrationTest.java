package com.covenantcode.crm.controller;

import com.covenantcode.crm.BaseIntegrationTest;
import com.covenantcode.crm.entity.Course;
import com.covenantcode.crm.entity.Lesson;
import com.covenantcode.crm.entity.Role;
import com.covenantcode.crm.entity.Student;
import com.covenantcode.crm.entity.StudyGroup;
import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.enums.GroupStatus;
import com.covenantcode.crm.entity.enums.RoleName;
import com.covenantcode.crm.repository.CourseRepository;
import com.covenantcode.crm.repository.LessonRepository;
import com.covenantcode.crm.repository.RoleRepository;
import com.covenantcode.crm.repository.StudentRepository;
import com.covenantcode.crm.repository.StudyGroupRepository;
import com.covenantcode.crm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StudyGroupLessonsControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private LessonRepository lessonRepository;

    private Course testCourse;
    private User teacher;
    private Student student1;
    private Student student2;
    private User teacher2;
    private StudyGroup group1;
    private StudyGroup group2;
    private User adminUser;
    private User managerUser;

    @BeforeEach
    void setUp() {
        Role teacherRole = roleRepository.findByName(RoleName.TEACHER)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(RoleName.TEACHER);
                    return roleRepository.save(newRole);
                });

        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(RoleName.STUDENT);
                    return roleRepository.save(newRole);
                });

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(RoleName.ADMIN);
                    return roleRepository.save(newRole);
                });

        Role managerRole = roleRepository.findByName(RoleName.MANAGER)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(RoleName.MANAGER);
                    return roleRepository.save(newRole);
                });

        testCourse = courseRepository.save(Course.builder()
                .title("Java for Test")
                .description("Integration test course")
                .durationInWeeks(8)
                .price(java.math.BigDecimal.valueOf(1000))
                .status(com.covenantcode.crm.entity.enums.CourseStatus.ACTIVE)
                .build());

        teacher = userRepository.save(User.builder()
                .firstName("Teacher")
                .lastName("Test")
                .email("teacher@test.com")
                .password("encoded_password")
                .role(teacherRole)
                .enabled(true)
                .build());

        student1 = studentRepository.save(Student.builder()
                .firstName("Student1")
                .lastName("Test")
                .email("student1@test.com")
                .phone("123456789")
                .build());

        student2 = studentRepository.save(Student.builder()
                .firstName("Student2")
                .lastName("Test")
                .email("student2@test.com")
                .phone("987654321")
                .build());

        group1 = StudyGroup.builder()
                .name("Morning Java")
                .course(testCourse)
                .teacher(teacher)
                .startDate(LocalDate.now())
                .status(GroupStatus.ACTIVE)
                .students(new HashSet<>(Set.of(student1, student2)))
                .build();

        group2 = StudyGroup.builder()
                .name("Evening Java")
                .course(testCourse)
                .teacher(teacher)
                .startDate(LocalDate.now().plusDays(7))
                .status(GroupStatus.ACTIVE)
                .students(new HashSet<>(Set.of(student2)))
                .build();
        studyGroupRepository.saveAll(List.of(group1, group2));

        User studentUser = User.builder()
                .email("student@test.com")
                .password("encoded_password")
                .firstName("StudentUser")
                .lastName("Test")
                .role(studentRole)
                .enabled(true)
                .build();
        userRepository.save(studentUser);
        student1.setUser(studentUser);
        studentRepository.save(student1);

        teacher2 = userRepository.save(User.builder()
                .firstName("Teacher2")
                .lastName("Test")
                .email("teacher2@test.com")
                .password("encoded_password")
                .role(teacherRole)
                .enabled(true)
                .build());

        adminUser = userRepository.save(User.builder()
                .email("admin@test.com")
                .password("password")
                .firstName("Admin")
                .lastName("Admin")
                .role(adminRole)
                .enabled(true)
                .build());

        managerUser = userRepository.save(User.builder()
                .email("manager@test.com")
                .password("password")
                .firstName("Manager")
                .lastName("Manager")
                .role(managerRole)
                .enabled(true)
                .build());
    }

    @Test
    @WithUserDetails(value = "admin@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("GET расписания активной группы → HTTP 200, список занятий отсортирован")
    void getLessons_ActiveGroup_ShouldReturnSortedList() throws Exception {

        Lesson lesson1 = new Lesson();
        lesson1.setStudyGroup(group1);
        lesson1.setTeacher(group1.getTeacher());
        lesson1.setTopic("Урок 3");
        lesson1.setLessonDate(LocalDate.of(2026, 6, 5)); // позже
        lesson1.setStartTime(LocalTime.of(18, 0));
        lesson1.setEndTime(LocalTime.of(19, 30));
        lessonRepository.save(lesson1);

        Lesson lesson2 = new Lesson();
        lesson2.setStudyGroup(group1);
        lesson2.setTeacher(group1.getTeacher());
        lesson2.setTopic("Урок 1");
        lesson2.setLessonDate(LocalDate.of(2026, 6, 2)); // раньше
        lesson2.setStartTime(LocalTime.of(18, 0));
        lesson2.setEndTime(LocalTime.of(19, 30));
        lessonRepository.save(lesson2);

        Lesson lesson3 = new Lesson();
        lesson3.setStudyGroup(group1);
        lesson3.setTeacher(group1.getTeacher());
        lesson3.setTopic("Урок 2");
        lesson3.setLessonDate(LocalDate.of(2026, 6, 2)); // тот же день, но другое время
        lesson3.setStartTime(LocalTime.of(19, 0));
        lesson3.setEndTime(LocalTime.of(20, 30));
        lessonRepository.save(lesson3);

        lessonRepository.flush();

        mockMvc.perform(get("/api/v1/groups/{groupId}/lessons", group1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].topic").value("Урок 1"))
                .andExpect(jsonPath("$[1].topic").value("Урок 2"))
                .andExpect(jsonPath("$[2].topic").value("Урок 3"));
    }

    @Test
    @WithUserDetails(value = "admin@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("группа не найдена → HTTP 404")
    void getLessons_GroupNotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/groups/999/lessons"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("resource-not-found"))
                .andExpect(jsonPath("$.detail").value("StudyGroup not found with id: 999"));
    }

    @Test
    @WithUserDetails(value = "teacher2@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("TEACHER не своя группа → HTTP 403")
    void getLessons_TeacherNotOwnGroup_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/groups/{groupId}/lessons", group1.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "student@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("STUDENT не своя группа → HTTP 403")
    void getLessons_StudentNotOwnGroup_ShouldReturn403() throws Exception {
        // Создаем группу, где нет студента
        StudyGroup otherGroup = new StudyGroup();
        otherGroup.setName("Other Group");
        otherGroup.setCourse(testCourse);
        otherGroup.setTeacher(teacher2);
        otherGroup.setStartDate(LocalDate.now());
        otherGroup.setStatus(GroupStatus.ACTIVE);
        otherGroup.setStudents(new HashSet<>());
        otherGroup = studyGroupRepository.save(otherGroup);

        mockMvc.perform(get("/api/v1/groups/{groupId}/lessons", otherGroup.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "student@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("STUDENT видит расписание своей группы → 200")
    void getLessons_StudentOwnGroup_ShouldReturn200() throws Exception {
        Lesson lesson = new Lesson();
        lesson.setStudyGroup(group1);
        lesson.setTeacher(group1.getTeacher());
        lesson.setTopic("Student's lesson");
        lesson.setLessonDate(LocalDate.now());
        lesson.setStartTime(LocalTime.of(10, 0));
        lesson.setEndTime(LocalTime.of(11, 30));
        lessonRepository.save(lesson);

        mockMvc.perform(get("/api/v1/groups/{groupId}/lessons", group1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].topic").value("Student's lesson"));
    }

    @Test
    @WithUserDetails(value = "teacher@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("TEACHER видит расписание своей группы → 200")
    void getLessons_TeacherOwnGroup_ShouldReturn200() throws Exception {
        Lesson lesson = new Lesson();
        lesson.setStudyGroup(group1);
        lesson.setTeacher(group1.getTeacher());
        lesson.setTopic("Teacher's lesson");
        lesson.setLessonDate(LocalDate.now());
        lesson.setStartTime(LocalTime.of(10, 0));
        lesson.setEndTime(LocalTime.of(11, 30));
        lessonRepository.save(lesson);

        mockMvc.perform(get("/api/v1/groups/{groupId}/lessons", group1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].topic").value("Teacher's lesson"));
    }

    @Test
    @WithUserDetails(value = "manager@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("MANAGER видит расписание любой группы → 200")
    void getLessons_ManagerAccess_ShouldReturn200() throws Exception {
        Lesson lesson = new Lesson();
        lesson.setStudyGroup(group1);
        lesson.setTeacher(group1.getTeacher());
        lesson.setTopic("Manager's lesson");
        lesson.setLessonDate(LocalDate.now());
        lesson.setStartTime(LocalTime.of(10, 0));
        lesson.setEndTime(LocalTime.of(11, 30));
        lessonRepository.save(lesson);

        mockMvc.perform(get("/api/v1/groups/{groupId}/lessons", group1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].topic").value("Manager's lesson"));
    }

    @Test
    @WithUserDetails(value = "admin@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("Группа существует, занятий нет → пустой список (200 OK)")
    void getLessons_NoLessons_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/groups/{groupId}/lessons", group2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(jsonPath("$").isArray());
    }
}