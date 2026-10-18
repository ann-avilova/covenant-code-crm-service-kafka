package com.covenantcode.crm.service.impl;

import com.covenantcode.crm.dto.student.StudentCreateRequest;
import com.covenantcode.crm.dto.student.StudentResponse;
import com.covenantcode.crm.dto.student.StudentUpdateRequest;
import com.covenantcode.crm.entity.Role;
import com.covenantcode.crm.entity.Student;
import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.enums.GroupStatus;
import com.covenantcode.crm.entity.enums.RoleName;
import com.covenantcode.crm.exception.ConflictException;
import com.covenantcode.crm.exception.ForbiddenException;
import com.covenantcode.crm.exception.ResourceNotFoundException;
import com.covenantcode.crm.mapper.StudentMapper;
import com.covenantcode.crm.repository.StudentRepository;
import com.covenantcode.crm.repository.StudyGroupRepository;
import com.covenantcode.crm.repository.UserRepository;
import com.covenantcode.crm.utils.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.eq;

import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private StudentServiceImpl studentService;

    private User adminUser;
    private User teacherUser;
    private User studentOwnerUser;
    private User anotherStudentUser;
    private Student student;
    private StudentResponse studentResponse;

    private User mockupUser;

    @BeforeEach
    void setUp() {

        mockupUser = new User();
        mockupUser.setId(1L);
        mockupUser.setEmail("admin@example.com");

        Role adminRole2 = new Role();
        adminRole2.setName(RoleName.ADMIN);

        mockupUser.setRole(adminRole2);

        Role adminRole = Role.builder().id(1L).name(RoleName.ADMIN).build();
        Role teacherRole = Role.builder().id(2L).name(RoleName.TEACHER).build();
        Role studentRole = Role.builder().id(3L).name(RoleName.STUDENT).build();

        adminUser = User.builder().id(10L).role(adminRole).build();
        teacherUser = User.builder().id(20L).role(teacherRole).build();
        studentOwnerUser = User.builder().id(30L).role(studentRole).build();
        anotherStudentUser = User.builder().id(40L).role(studentRole).build();

        student = Student.builder()
                .id(1L)
                .firstName("Ivan")
                .lastName("Ivanov")
                .user(studentOwnerUser)
                .build();

        studentResponse = StudentResponse.builder()
                .id(1L)
                .firstName("Ivan")
                .userId(30L)
                .build();
    }

    @Test
    void getById_asTeacher_whenNotAllowed_shouldThrowForbiddenException() {
        when(currentUserProvider.getCurrentUser()).thenReturn(teacherUser);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByIdAndStudyGroupsTeacherId(1L, teacherUser.getId())).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> studentService.getById(1L));

        verify(currentUserProvider).getCurrentUser();
        verify(studentRepository).findById(1L);
        verify(studentRepository).existsByIdAndStudyGroupsTeacherId(1L, teacherUser.getId());
        verify(studentMapper, never()).toResponse(any());
    }

    @Test
    void getById_asTeacher_whenAllowed_shouldReturnStudent() {
        when(currentUserProvider.getCurrentUser()).thenReturn(teacherUser);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByIdAndStudyGroupsTeacherId(1L, teacherUser.getId())).thenReturn(true);
        when(studentMapper.toResponse(student)).thenReturn(studentResponse);

        StudentResponse result = studentService.getById(1L);

        assertNotNull(result);
        assertEquals(studentResponse, result);

        verify(currentUserProvider).getCurrentUser();
        verify(studentRepository).findById(1L);
        verify(studentRepository).existsByIdAndStudyGroupsTeacherId(1L, teacherUser.getId());
        verify(studentMapper).toResponse(student);
    }

    @Test
    void getById_whenNotFound_shouldThrowResourceNotFoundException() {
        when(currentUserProvider.getCurrentUser()).thenReturn(adminUser);
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.getById(99L));

        verify(currentUserProvider).getCurrentUser();
        verify(studentRepository).findById(99L);
        verify(studentMapper, never()).toResponse(any());
    }


    @Test
    @DisplayName("Тест 1: Успешное создание студента без привязки к пользователю")
    void create_WithoutUserId_ShouldSucceed() {

        StudentCreateRequest request = StudentCreateRequest.builder()
                .firstName("Ivan")
                .lastName("Ivanov")
                .userId(null)
                .build();

        Student savedStudent = Student.builder().id(1L).firstName("Ivan").build();
        StudentResponse expectedResponse = new StudentResponse();
        expectedResponse.setId(1L);
        expectedResponse.setUserId(null);

        when(studentRepository.saveAndFlush(any(Student.class))).thenReturn(savedStudent);
        when(studentMapper.toResponse(savedStudent)).thenReturn(expectedResponse);

        StudentResponse actualResponse = studentService.create(request);

        assertNotNull(actualResponse);
        assertEquals(1L, actualResponse.getId());
        assertNull(actualResponse.getUserId());

        verify(userRepository, never()).findById(any());
        verify(studentRepository).saveAndFlush(any(Student.class));
    }

    @Test
    @DisplayName("Тест 2: Успешное создание студента с валидным userId")
    void create_WithValidUserId_ShouldSucceed() {

        Long userId = 5L;
        StudentCreateRequest request = StudentCreateRequest.builder()
                .firstName("Petr")
                .userId(userId)
                .build();

        User user = new User();
        user.setId(userId);

        Student savedStudent = Student.builder().id(10L).user(user).build();
        StudentResponse expectedResponse = new StudentResponse();
        expectedResponse.setId(10L);
        expectedResponse.setUserId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(studentRepository.existsByUser_Id(userId)).thenReturn(false);
        when(studentRepository.saveAndFlush(any(Student.class))).thenReturn(savedStudent);
        when(studentMapper.toResponse(savedStudent)).thenReturn(expectedResponse);

        StudentResponse actualResponse = studentService.create(request);

        assertEquals(userId, actualResponse.getUserId());
        verify(userRepository).findById(userId);
        verify(studentRepository).existsByUser_Id(userId);
    }

    @Test
    @DisplayName("Тест 3: Ошибка, если указанный userId не существует")
    void create_WithNonExistentUserId_ShouldThrowNotFound() {

        Long userId = 99L;
        StudentCreateRequest request = StudentCreateRequest.builder().userId(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.create(request));

        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Тест 4: Ошибка, если пользователь уже привязан к другому студенту")
    void create_WithAlreadyTakenUserId_ShouldThrowConflict() {

        Long userId = 5L;
        StudentCreateRequest request = StudentCreateRequest.builder().userId(userId).build();

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(studentRepository.existsByUser_Id(userId)).thenReturn(true);

        assertThrows(ConflictException.class, () -> studentService.create(request));

        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Список всех студентов без поиска — возвращает всех студентов постранично")
    void getAll_WithoutSearch_ShouldReturnAllStudentsPaginated() {
        Pageable pageable = PageRequest.of(0, 20);

        when(currentUserProvider.getCurrentUser()).thenReturn(mockupUser);

        Page<Student> studentPage = new PageImpl<>(List.of(student), pageable, 1);
        when(studentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(studentPage);
        when(studentMapper.toResponse(any())).thenReturn(studentResponse);

        Page<StudentResponse> result = studentService.getAll(null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(currentUserProvider).getCurrentUser();
        verify(studentRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Поиск по строке — возвращает отфильтрованных студентов")
    void getAll_WithSearch_ShouldReturnFilteredStudents() {

        String search = "Смир";
        Pageable pageable = PageRequest.of(0, 20);

        when(currentUserProvider.getCurrentUser()).thenReturn(mockupUser);

        Student student1 = new Student();
        student1.setId(1L);
        student1.setFirstName("Алиса");
        student1.setLastName("Смирнова");

        List<Student> students = List.of(student1);
        Page<Student> studentPage = new PageImpl<>(students, pageable, students.size());

        StudentResponse response1 = new StudentResponse();
        response1.setId(1L);
        response1.setFirstName("Алиса");
        response1.setLastName("Смирнова");

        when(studentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(studentPage);
        when(studentMapper.toResponse(student1)).thenReturn(response1);

        Page<StudentResponse> result = studentService.getAll(search, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).containsExactly(response1);

        verify(studentRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        verify(studentMapper, times(1)).toResponse(student1);
    }

    @Test
    @DisplayName("Пустой список студентов — возвращает пустую страницу")
    void getAll_WhenNoStudents_ShouldReturnEmptyPage() {

        Pageable pageable = PageRequest.of(0, 20);

        when(currentUserProvider.getCurrentUser()).thenReturn(mockupUser);

        List<Student> emptyList = List.of();
        Page<Student> emptyPage = new PageImpl<>(emptyList, pageable, 0);

        when(studentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        Page<StudentResponse> result = studentService.getAll(null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();

        verify(studentRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        verifyNoInteractions(studentMapper);
    }

    @Test
    @DisplayName("Тест: успешное обновление студента (200)")
    void update_ValidRequest_ShouldSucceed() {
        Long studentId = 1L;
        Student existingStudent = Student.builder()
                .id(studentId)
                .firstName("Old")
                .lastName("Student")
                .phone("123")
                .email("old@example.com")
                .birthDate(LocalDate.of(2000, 1, 1))
                .build();

        StudentUpdateRequest request = StudentUpdateRequest.builder()
                .firstName("Ivan")
                .lastName("Ivanov")
                .phone("+79123456789")
                .email("ivan@example.com")
                .birthDate(LocalDate.of(1995, 5, 15))
                .build();

        Student updatedStudent = Student.builder()
                .id(studentId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .birthDate(request.getBirthDate())
                .build();

        StudentResponse expectedResponse = StudentResponse.builder()
                .id(studentId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .birthDate(request.getBirthDate())
                .build();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(existingStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(updatedStudent);
        when(studentMapper.toResponse(updatedStudent)).thenReturn(expectedResponse);

        StudentResponse actualResponse = studentService.update(studentId, request);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getId(), actualResponse.getId());
        assertEquals(request.getFirstName(), actualResponse.getFirstName());
        assertEquals(request.getLastName(), actualResponse.getLastName());
        assertEquals(request.getPhone(), actualResponse.getPhone());
        assertEquals(request.getEmail(), actualResponse.getEmail());
        assertEquals(request.getBirthDate(), actualResponse.getBirthDate());

        assertEquals(request.getFirstName(), existingStudent.getFirstName());
        assertEquals(request.getLastName(), existingStudent.getLastName());
        assertEquals(request.getPhone(), existingStudent.getPhone());
        assertEquals(request.getEmail(), existingStudent.getEmail());
        assertEquals(request.getBirthDate(), existingStudent.getBirthDate());

        verify(studentRepository).findById(studentId);
        verify(studentRepository).save(existingStudent);
        verify(studentMapper).toResponse(updatedStudent);
        verifyNoMoreInteractions(studentRepository, studentMapper);
    }

    @Test
    @DisplayName("Тест: студент не найден (404)")
    void update_StudentNotFound_ShouldThrowResourceNotFoundException() {
        Long studentId = 99L;
        StudentUpdateRequest request = StudentUpdateRequest.builder()
                .firstName("Ivan")
                .lastName("Ivanov")
                .phone("+79123456789")
                .build();

        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.update(studentId, request));

        verify(studentRepository).findById(studentId);
        verify(studentRepository, never()).save(any());
        verifyNoInteractions(studentMapper);
    }

    @Test
    @DisplayName("Тест: обновление не трогает поле userId (привязка к пользователю сохраняется)")
    void update_ShouldNotChangeUserAssociation() {
        Long studentId = 1L;
        Long userId = 5L;
        User user = new User();
        user.setId(userId);

        Student existingStudent = Student.builder()
                .id(studentId)
                .firstName("Old")
                .lastName("Student")
                .phone("123")
                .user(user)
                .build();

        StudentUpdateRequest request = StudentUpdateRequest.builder()
                .firstName("New")
                .lastName("Name")
                .phone("+79000000000")
                .build();

        User originalUser = existingStudent.getUser();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(existingStudent));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(studentMapper.toResponse(any(Student.class))).thenReturn(StudentResponse.builder().build());

        studentService.update(studentId, request);

        assertNotNull(existingStudent.getUser());
        assertEquals(originalUser, existingStudent.getUser());
        assertEquals(userId, existingStudent.getUser().getId());

        assertEquals(request.getFirstName(), existingStudent.getFirstName());
        assertEquals(request.getLastName(), existingStudent.getLastName());
        assertEquals(request.getPhone(), existingStudent.getPhone());

        verify(studentRepository).save(existingStudent);
    }

    @Test
    @DisplayName("Успешное удаление студента (204) - студент существует и не состоит в активной группе")
    void deleteById_ShouldDeleteStudent_WhenStudentExistsAndNotInActiveGroup() {

        Long studentId = 1L;
        Student student = new Student();
        student.setId(studentId);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studyGroupRepository.existsByStudents_IdAndStatus(eq(studentId), eq(GroupStatus.ACTIVE)))
                .thenReturn(false);

        assertDoesNotThrow(() -> studentService.deleteById(studentId));

        verify(studentRepository, times(1)).findById(studentId);
        verify(studyGroupRepository, times(1))
                .existsByStudents_IdAndStatus(eq(studentId), eq(GroupStatus.ACTIVE));
        verify(studentRepository, times(1)).delete(student);
    }

    @Test
    @DisplayName("Ошибка 404 - студент не найден")
    void deleteById_ShouldThrowResourceNotFoundException_WhenStudentNotFound() {

        Long studentId = 99L;

        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentService.deleteById(studentId)
        );

        assertEquals("Student с id 99 не найден", exception.getMessage());

        verify(studentRepository, times(1)).findById(studentId);
        verify(studyGroupRepository, never()).existsByStudents_IdAndStatus(anyLong(), any(GroupStatus.class));
        verify(studentRepository, never()).delete(any(Student.class));
    }

    @Test
    @DisplayName("Ошибка 409 - студент состоит в активной группе")
    void deleteById_ShouldThrowConflictException_WhenStudentInActiveGroup() {

        Long studentId = 1L;
        Student student = new Student();
        student.setId(studentId);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studyGroupRepository.existsByStudents_IdAndStatus(eq(studentId), eq(GroupStatus.ACTIVE)))
                .thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> studentService.deleteById(studentId)
        );

        assertEquals(
                "Студент с id 1 состоит в активной учебной группе и не может быть удалён",
                exception.getMessage()
        );

        verify(studentRepository, times(1)).findById(studentId);
        verify(studyGroupRepository, times(1))
                .existsByStudents_IdAndStatus(eq(studentId), eq(GroupStatus.ACTIVE));
        verify(studentRepository, never()).delete(any(Student.class));
    }
}
