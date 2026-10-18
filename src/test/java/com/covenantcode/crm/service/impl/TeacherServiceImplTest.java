package com.covenantcode.crm.service.impl;

import com.covenantcode.crm.dto.teacher.TeacherCreateRequest;
import com.covenantcode.crm.dto.teacher.TeacherResponse;
import com.covenantcode.crm.dto.teacher.TeacherUpdateRequest;
import com.covenantcode.crm.entity.Role;
import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.enums.RoleName;
import com.covenantcode.crm.exception.ConflictException;
import com.covenantcode.crm.exception.ForbiddenException;
import com.covenantcode.crm.exception.ResourceNotFoundException;
import com.covenantcode.crm.mapper.TeacherMapper;
import com.covenantcode.crm.repository.RoleRepository;
import com.covenantcode.crm.repository.StudyGroupRepository;
import com.covenantcode.crm.repository.UserRepository;
import com.covenantcode.crm.utils.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeacherServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeacherMapper teacherMapper;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @InjectMocks
    private TeacherServiceImpl teacherService;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private Role teacherRole;
    private Role adminRole;
    private User teacher;
    private User anotherTeacher;
    private User admin;
    private TeacherResponse teacherResponse;

    @BeforeEach
    void setUp() {
        teacherRole = Role.builder()
                .id(1L)
                .name(RoleName.TEACHER)
                .build();

        adminRole = Role.builder()
                .id(2L)
                .name(RoleName.ADMIN)
                .build();

        teacher = User.builder()
                .id(1L)
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("teacher@test.com")
                .phone("+998901111111")
                .password("encoded-password")
                .enabled(true)
                .role(teacherRole)
                .build();

        anotherTeacher = User.builder()
                .id(2L)
                .firstName("Petr")
                .lastName("Petrov")
                .email("another.teacher@test.com")
                .phone("+998902222222")
                .password("encoded-password")
                .enabled(true)
                .role(teacherRole)
                .build();

        admin = User.builder()
                .id(10L)
                .firstName("Admin")
                .lastName("Adminov")
                .email("admin@test.com")
                .phone("+998909999999")
                .password("encoded-password")
                .enabled(true)
                .role(adminRole)
                .build();

        teacherResponse = TeacherResponse.builder()
                .id(1L)
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("teacher@test.com")
                .phone("+998901111111")
                .enabled(true)
                .build();
    }

    @Test
    void getAll_shouldReturnTeachersForAdmin() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> usersPage = new PageImpl<>(List.of(teacher), pageable, 1);

        when(currentUserProvider.getCurrentUser()).thenReturn(admin);
        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(usersPage);
        when(teacherMapper.toResponse(teacher)).thenReturn(teacherResponse);

        Page<TeacherResponse> result = teacherService.getAll(null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(teacherResponse, result.getContent().get(0));

        verify(currentUserProvider).getCurrentUser();
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
        verify(teacherMapper).toResponse(teacher);
    }

    @Test
    void getAll_shouldReturnFilteredTeachersForTeacher() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> usersPage = new PageImpl<>(List.of(teacher), pageable, 1);

        when(currentUserProvider.getCurrentUser()).thenReturn(teacher);
        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(usersPage);
        when(teacherMapper.toResponse(teacher)).thenReturn(teacherResponse);

        Page<TeacherResponse> result = teacherService.getAll("Ivan", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(teacherResponse, result.getContent().get(0));

        verify(currentUserProvider).getCurrentUser();
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
        verify(teacherMapper).toResponse(teacher);
    }

    @Test
    void getAll_shouldThrowResourceNotFoundException_whenCurrentUserNotFound() {
        Pageable pageable = PageRequest.of(0, 10);

        when(currentUserProvider.getCurrentUser())
                .thenThrow(new ResourceNotFoundException("Текущий пользователь не найден"));

        assertThrows(
                ResourceNotFoundException.class,
                () -> teacherService.getAll(null, pageable)
        );

        verify(currentUserProvider).getCurrentUser();
        verify(userRepository, never()).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void create_shouldCreateTeacher() {
        TeacherCreateRequest request = TeacherCreateRequest.builder()
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("teacher@test.com")
                .phone("+998901111111")
                .password("password")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(roleRepository.findByName(RoleName.TEACHER)).thenReturn(Optional.of(teacherRole));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(teacher);
        when(teacherMapper.toResponse(teacher)).thenReturn(teacherResponse);

        TeacherResponse result = teacherService.create(request);

        assertNotNull(result);
        assertEquals(teacherResponse, result);

        verify(userRepository).existsByEmail(request.getEmail());
        verify(roleRepository).findByName(RoleName.TEACHER);
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).saveAndFlush(any(User.class));
        verify(teacherMapper).toResponse(teacher);
    }

    @Test
    void create_shouldThrowConflictException_whenEmailAlreadyExists() {
        TeacherCreateRequest request = TeacherCreateRequest.builder()
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("teacher@test.com")
                .phone("+998901111111")
                .password("password")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> teacherService.create(request)
        );

        verify(userRepository).existsByEmail(request.getEmail());
        verify(roleRepository, never()).findByName(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void create_shouldThrowResourceNotFoundException_whenTeacherRoleNotFound() {
        TeacherCreateRequest request = TeacherCreateRequest.builder()
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("teacher@test.com")
                .phone("+998901111111")
                .password("password")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(roleRepository.findByName(RoleName.TEACHER)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> teacherService.create(request)
        );

        verify(userRepository).existsByEmail(request.getEmail());
        verify(roleRepository).findByName(RoleName.TEACHER);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void delete_shouldDeleteTeacher() {
        when(userRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
        when(studyGroupRepository.countByTeacherId(teacher.getId())).thenReturn(0L);

        assertDoesNotThrow(() -> teacherService.delete(teacher.getId()));

        verify(userRepository).findById(teacher.getId());
        verify(studyGroupRepository).countByTeacherId(teacher.getId());
        verify(userRepository).delete(teacher);
    }

    @Test
    void delete_shouldThrowResourceNotFoundException_whenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> teacherService.delete(999L)
        );

        verify(userRepository).findById(999L);
        verify(studyGroupRepository, never()).countByTeacherId(any());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void delete_shouldThrowResourceNotFoundException_whenUserIsNotTeacher() {
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        assertThrows(
                ResourceNotFoundException.class,
                () -> teacherService.delete(admin.getId())
        );

        verify(userRepository).findById(admin.getId());
        verify(studyGroupRepository, never()).countByTeacherId(any());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void delete_shouldThrowConflictException_whenTeacherHasGroups() {
        when(userRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
        when(studyGroupRepository.countByTeacherId(teacher.getId())).thenReturn(2L);

        assertThrows(
                ConflictException.class,
                () -> teacherService.delete(teacher.getId())
        );

        verify(userRepository).findById(teacher.getId());
        verify(studyGroupRepository).countByTeacherId(teacher.getId());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void getById_shouldReturnTeacherForAdmin() {
        when(currentUserProvider.getCurrentUser()).thenReturn(admin);
        when(userRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
        when(teacherMapper.toResponse(teacher)).thenReturn(teacherResponse);

        TeacherResponse result = teacherService.getById(teacher.getId());

        assertNotNull(result);
        assertEquals(teacherResponse, result);

        verify(currentUserProvider).getCurrentUser();
        verify(userRepository).findById(teacher.getId());
        verify(teacherMapper).toResponse(teacher);
    }

    @Test
    void getById_shouldReturnOwnTeacherCardForTeacher() {
        when(currentUserProvider.getCurrentUser()).thenReturn(teacher);
        when(userRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
        when(teacherMapper.toResponse(teacher)).thenReturn(teacherResponse);

        TeacherResponse result = teacherService.getById(teacher.getId());

        assertNotNull(result);
        assertEquals(teacherResponse, result);

        verify(currentUserProvider).getCurrentUser();
        verify(userRepository).findById(teacher.getId());
        verify(teacherMapper).toResponse(teacher);
    }

    @Test
    void getById_shouldThrowForbiddenException_whenTeacherRequestsAnotherTeacherCard() {
        when(currentUserProvider.getCurrentUser()).thenReturn(teacher);

        assertThrows(
                ForbiddenException.class,
                () -> teacherService.getById(anotherTeacher.getId())
        );

        verify(currentUserProvider).getCurrentUser();
        verify(userRepository, never()).findById(anotherTeacher.getId());
        verify(teacherMapper, never()).toResponse(any(User.class));
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenCurrentUserNotFound() {
        when(currentUserProvider.getCurrentUser())
                .thenThrow(new ResourceNotFoundException("Текущий пользователь не найден"));

        assertThrows(
                ResourceNotFoundException.class,
                () -> teacherService.getById(teacher.getId())
        );

        verify(currentUserProvider).getCurrentUser();
        verify(userRepository, never()).findById(any());
        verify(teacherMapper, never()).toResponse(any(User.class));
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenTeacherNotFound() {
        when(currentUserProvider.getCurrentUser()).thenReturn(admin);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> teacherService.getById(999L)
        );

        verify(currentUserProvider).getCurrentUser();
        verify(userRepository).findById(999L);
        verify(teacherMapper, never()).toResponse(any(User.class));
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenFoundUserIsNotTeacher() {
        User manager = User.builder()
                .id(20L)
                .firstName("Manager")
                .lastName("Managerov")
                .email("manager@test.com")
                .enabled(true)
                .role(adminRole)
                .build();

        when(currentUserProvider.getCurrentUser()).thenReturn(admin);
        when(userRepository.findById(manager.getId())).thenReturn(Optional.of(manager));

        assertThrows(
                ResourceNotFoundException.class,
                () -> teacherService.getById(manager.getId())
        );

        verify(currentUserProvider).getCurrentUser();
        verify(userRepository).findById(manager.getId());
        verify(teacherMapper, never()).toResponse(any(User.class));
    }

    @Test
    void update_shouldUpdateTeacher() {
        TeacherUpdateRequest request = TeacherUpdateRequest.builder()
                .firstName("Updated")
                .lastName("Teacher")
                .phone("+998903333333")
                .build();

        TeacherResponse updatedResponse = TeacherResponse.builder()
                .id(teacher.getId())
                .firstName("Updated")
                .lastName("Teacher")
                .email(teacher.getEmail())
                .phone("+998903333333")
                .enabled(true)
                .build();

        when(userRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
        when(userRepository.saveAndFlush(teacher)).thenReturn(teacher);
        when(teacherMapper.toResponse(teacher)).thenReturn(updatedResponse);

        TeacherResponse result = teacherService.update(teacher.getId(), request);

        assertNotNull(result);
        assertEquals(updatedResponse, result);
        assertEquals("Updated", teacher.getFirstName());
        assertEquals("Teacher", teacher.getLastName());
        assertEquals("+998903333333", teacher.getPhone());

        verify(userRepository).findById(teacher.getId());
        verify(userRepository).saveAndFlush(teacher);
        verify(teacherMapper).toResponse(teacher);
    }

    @Test
    void update_shouldThrowResourceNotFoundException_whenUserNotFound() {
        TeacherUpdateRequest request = TeacherUpdateRequest.builder()
                .firstName("Updated")
                .lastName("Teacher")
                .phone("+998903333333")
                .build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> teacherService.update(999L, request)
        );

        verify(userRepository).findById(999L);
        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(teacherMapper, never()).toResponse(any(User.class));
    }

    @Test
    void update_shouldThrowResourceNotFoundException_whenUserIsNotTeacher() {
        TeacherUpdateRequest request = TeacherUpdateRequest.builder()
                .firstName("Updated")
                .lastName("Teacher")
                .phone("+998903333333")
                .build();

        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        assertThrows(
                ResourceNotFoundException.class,
                () -> teacherService.update(admin.getId(), request)
        );

        verify(userRepository).findById(admin.getId());
        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(teacherMapper, never()).toResponse(any(User.class));
    }

    @Test
    void setEnabled_shouldSetTeacherEnabled() {
        when(userRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
        when(userRepository.saveAndFlush(teacher)).thenReturn(teacher);
        when(teacherMapper.toResponse(teacher)).thenReturn(teacherResponse);

        TeacherResponse result = teacherService.setEnabled(teacher.getId(), false);

        assertNotNull(result);
        assertEquals(teacherResponse, result);
        assertEquals(false, teacher.isEnabled());

        verify(userRepository).findById(teacher.getId());
        verify(userRepository).saveAndFlush(teacher);
        verify(teacherMapper).toResponse(teacher);
    }

    @Test
    void setEnabled_shouldThrowResourceNotFoundException_whenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> teacherService.setEnabled(999L, true)
        );

        verify(userRepository).findById(999L);
        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(teacherMapper, never()).toResponse(any(User.class));
    }

    @Test
    void setEnabled_shouldThrowResourceNotFoundException_whenUserIsNotTeacher() {
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        assertThrows(
                ResourceNotFoundException.class,
                () -> teacherService.setEnabled(admin.getId(), true)
        );

        verify(userRepository).findById(admin.getId());
        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(teacherMapper, never()).toResponse(any(User.class));
    }
}
