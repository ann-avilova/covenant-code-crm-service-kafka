package com.covenantcode.crm.service.impl;

import com.covenantcode.crm.dto.teacher.TeacherCreateRequest;
import com.covenantcode.crm.dto.teacher.TeacherUpdateRequest;
import com.covenantcode.crm.dto.teacher.TeacherResponse;
import com.covenantcode.crm.entity.Role;
import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.enums.RoleName;
import com.covenantcode.crm.exception.ConflictException;
import com.covenantcode.crm.exception.ForbiddenException;
import com.covenantcode.crm.exception.ResourceNotFoundException;
import com.covenantcode.crm.mapper.TeacherMapper;
import com.covenantcode.crm.repository.RoleRepository;
import com.covenantcode.crm.repository.StudyGroupRepository;
import com.covenantcode.crm.repository.TeacherSpecifications;
import com.covenantcode.crm.repository.UserRepository;
import com.covenantcode.crm.service.TeacherService;
import com.covenantcode.crm.utils.CurrentUserProvider;
import com.covenantcode.crm.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final UserRepository userRepository;
    private final TeacherMapper teacherMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudyGroupRepository studyGroupRepository;

    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    @Override
    public Page<TeacherResponse> getAll(
            String search,
            Pageable pageable
    ) {
        User currentUser = currentUserProvider.getCurrentUser();

        Specification<User> spec = Specification.where(TeacherSpecifications.hasTeacherRole())
                .and(TeacherSpecifications.searchByText(search));

        if (currentUser.getRole().getName() == RoleName.TEACHER) {
            spec = spec.and(TeacherSpecifications.teachesSameCoursesAs(currentUser.getId()));
        }

        return userRepository.findAll(spec, pageable)
                .map(teacherMapper::toResponse);
    }

    @Override
    @Transactional
    public TeacherResponse create(TeacherCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Пользователь с email " + request.getEmail() + " уже существует");
        }

        Role role = roleRepository.findByName(RoleName.TEACHER).orElseThrow(() -> new ResourceNotFoundException("Роль TEACHER не найдена"));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .role(role)
                .build();

        User savedUser = userRepository.saveAndFlush(user);
        return teacherMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Преподаватель с id " + id + " не найден"));

        if (!Utils.isOnlyTeacher(user)) {
            throw new ResourceNotFoundException("Преподаватель с id " + id + " не найден");
        }

        long groupCount = studyGroupRepository.countByTeacherId(id);
        if (groupCount > 0) {
            throw new ConflictException(
                    String.format("Невозможно удалить преподавателя: у него %d групп(ы). Сначала переназначьте группы.", groupCount)
            );
        }

        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    @Override
    public TeacherResponse getById(
            Long id
    ) {

        User currentUser = currentUserProvider.getCurrentUser();

        if (currentUser.getRole().getName() == RoleName.TEACHER
                && !currentUser.getId().equals(id)) {
            throw new ForbiddenException("Вы можете просматривать только свою карточку");
        }

        User teacher = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Преподаватель не найден"));

        if (teacher.getRole().getName() != RoleName.TEACHER) {
            throw new ResourceNotFoundException("Преподаватель не найден");
        }

        return teacherMapper.toResponse(teacher);
    }

    @Override
    @Transactional
    public TeacherResponse update(Long id, TeacherUpdateRequest request) {
        User user = findTeacherById(id);

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());

        User updatedUser = userRepository.saveAndFlush(user);
        return teacherMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public TeacherResponse setEnabled(Long id, boolean enabled) {
        User user = findTeacherById(id);

        user.setEnabled(enabled);

        User updatedUser = userRepository.saveAndFlush(user);
        return teacherMapper.toResponse(updatedUser);
    }

    private User findTeacherById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Преподаватель с id " + id + " не найден"));

        if (user.getRole() == null || !RoleName.TEACHER.equals(user.getRole().getName())) {
            throw new ResourceNotFoundException("Преподаватель с id " + id + " не найден");
        }

        return user;
    }
}
