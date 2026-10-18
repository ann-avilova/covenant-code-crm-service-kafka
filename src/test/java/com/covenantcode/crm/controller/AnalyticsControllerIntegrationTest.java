package com.covenantcode.crm.controller;
import com.covenantcode.crm.BaseIntegrationTest;
import com.covenantcode.crm.entity.Course;
import com.covenantcode.crm.entity.Lead;
import com.covenantcode.crm.entity.Role;
import com.covenantcode.crm.entity.Student;
import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.enums.CourseStatus;
import com.covenantcode.crm.entity.enums.LeadStatus;
import com.covenantcode.crm.entity.enums.RoleName;
import com.covenantcode.crm.repository.CourseRepository;
import com.covenantcode.crm.repository.LeadRepository;
import com.covenantcode.crm.repository.LessonRepository;
import com.covenantcode.crm.repository.RoleRepository;
import com.covenantcode.crm.repository.StudentRepository;
import com.covenantcode.crm.repository.StudyGroupRepository;
import com.covenantcode.crm.repository.UserRepository;
import com.covenantcode.crm.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AnalyticsControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String teacherToken;

    private final String baseUrl = "/api/v1/analytics/dashboard";

    @BeforeEach
    void setUp() {
        // 1. Очищаем репозитории в правильном порядке
        lessonRepository.deleteAll();
        studyGroupRepository.deleteAll();
        studentRepository.deleteAll();
        leadRepository.deleteAll();
        userRepository.deleteAll();
        courseRepository.deleteAll();

        // 2. Получаем или создаем обязательные роли
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName(RoleName.ADMIN);
                    return roleRepository.save(r);
                });

        Role teacherRole = roleRepository.findByName(RoleName.TEACHER)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName(RoleName.TEACHER);
                    return roleRepository.save(r);
                });

        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName(RoleName.STUDENT);
                    return roleRepository.save(r);
                });

        // 3. Создаем пользователей для авторизации
        User testAdmin = new User();
        testAdmin.setFirstName("Admin");
        testAdmin.setLastName("Test");
        testAdmin.setEmail("admin.analytics@test.com");
        testAdmin.setPassword(passwordEncoder.encode("password"));
        testAdmin.setRole(adminRole);
        testAdmin.setEnabled(true);
        testAdmin = userRepository.save(testAdmin);

        User testTeacher = new User();
        testTeacher.setFirstName("Teacher");
        testTeacher.setLastName("Test");
        testTeacher.setEmail("teacher.analytics@test.com");
        testTeacher.setPassword(passwordEncoder.encode("password"));
        testTeacher.setRole(teacherRole);
        testTeacher.setEnabled(true);
        testTeacher = userRepository.save(testTeacher);

        // 4. Создаем активный курс для корректной работы счетчика
        Course testCourse = new Course();
        testCourse.setTitle("Test Course");
        testCourse.setDescription("Integration test course");
        testCourse.setPrice(BigDecimal.valueOf(199.99));
        testCourse.setDurationInWeeks(8);
        testCourse.setStatus(CourseStatus.ACTIVE);
        courseRepository.save(testCourse);

        // 5. Наполняем БД аналитическими данными лидов
        Lead lead = new Lead();
        lead.setStatus(LeadStatus.CONVERTED_TO_STUDENT);
        lead.setFirstName("Иван");
        lead.setLastName("Иванов");
        lead.setPhone("+79991112233");
        lead.setEmail("lead.test@example.com");
        leadRepository.save(lead);

        // 6. Создаем пользователя Студента и саму сущность Студента
        User studentUser = new User();
        studentUser.setFirstName("Student");
        studentUser.setLastName("Test");
        studentUser.setEmail("student.analytics@test.com");
        studentUser.setPassword(passwordEncoder.encode("password"));
        studentUser.setRole(studentRole);
        studentUser.setEnabled(true);
        studentUser = userRepository.save(studentUser);

        Student student = new Student();
        student.setUser(studentUser);
        student.setFirstName("Петр");
        student.setLastName("Петров");
        student.setEmail("student.profile@test.com");
        student.setPhone("+79992223344");
        studentRepository.save(student);

        // 7. Генерируем JWT токены
        adminToken = jwtService.generateToken(testAdmin);
        teacherToken = jwtService.generateToken(testTeacher);
    }

    @Test
    @DisplayName("Тест 1: ADMIN получает дашборд (200)")
    void getDashboard_AsAdmin_ReturnsOkAndData() throws Exception {
        mockMvc.perform(get(baseUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLeads").value(1))
                .andExpect(jsonPath("$.activeCourses").value(1))
                .andExpect(jsonPath("$.leadsByStatus").exists())
                .andExpect(jsonPath("$.leadsByStatus.CONVERTED_TO_STUDENT").value(1))
                .andExpect(jsonPath("$.totalStudents").value(1));
    }

    @Test
    @DisplayName("Тест 2: TEACHER не получает дашборд (403)")
    void getDashboard_AsTeacher_ReturnsForbidden() throws Exception {
        mockMvc.perform(get(baseUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}