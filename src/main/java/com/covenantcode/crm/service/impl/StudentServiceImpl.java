package com.covenantcode.crm.service.impl;

import com.covenantcode.crm.dto.student.StudentCreateRequest;
import com.covenantcode.crm.dto.student.StudentResponse;
import com.covenantcode.crm.dto.student.StudentUpdateRequest;
import com.covenantcode.crm.entity.Student;
import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.enums.GroupStatus;
import com.covenantcode.crm.entity.enums.RoleName;
import com.covenantcode.crm.exception.ConflictException;
import com.covenantcode.crm.exception.ForbiddenException;
import com.covenantcode.crm.exception.ResourceNotFoundException;
import com.covenantcode.crm.mapper.StudentMapper;
import com.covenantcode.crm.repository.StudentRepository;
import com.covenantcode.crm.repository.StudentSpecifications;
import com.covenantcode.crm.repository.StudyGroupRepository;
import com.covenantcode.crm.repository.UserRepository;
import com.covenantcode.crm.service.StudentService;
import com.covenantcode.crm.utils.CurrentUserProvider;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@AllArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;


    @Transactional(readOnly = true)
    @Override
    public StudentResponse getById(Long id) {
        User currentUser = currentUserProvider.getCurrentUser();

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Студент не найден"));

        if (currentUser.getRole().getName() == RoleName.TEACHER) {
            boolean allowed = studentRepository.existsByIdAndStudyGroupsTeacherId(id, currentUser.getId());

            if (!allowed) {
                throw new ForbiddenException("Вы можете просматривать только студентов своих групп");
            }
        }

        if (currentUser.getRole().getName() == RoleName.STUDENT) {
            if (student.getUser() == null || !student.getUser().getId().equals(currentUser.getId())) {
                throw new ForbiddenException("Вы можете просматривать только свой профиль");
            }
        }

        return studentMapper.toResponse(student);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> getAll(String search, Pageable pageable) {

        User currentUser = currentUserProvider.getCurrentUser();


        Specification<Student> spec = Specification.where(StudentSpecifications.searchByText(search));

        if (currentUser.getRole().getName() == RoleName.TEACHER) {
            spec = spec.and(StudentSpecifications.belongsToTeacherGroups(currentUser.getId()));
        }

        return studentRepository.findAll(spec, pageable)
                .map(studentMapper::toResponse);
    }

    @Override
    @Transactional
    public StudentResponse create(StudentCreateRequest request) {
        User user = null;

        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

            if (studentRepository.existsByUser_Id(request.getUserId())) {
                throw new ConflictException(
                        String.format("Пользователь с id %d уже привязан к другому студенту", request.getUserId())
                );
            }
        }

        Student student = Student.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .birthDate(request.getBirthDate())
                .user(user)
                .build();

        Student savedStudent = studentRepository.saveAndFlush(student);

        return studentMapper.toResponse(savedStudent);
    }

    @Override
    @Transactional
    public StudentResponse update(Long id, StudentUpdateRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student с id " + id + " не найден"));

        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setPhone(request.getPhone());
        student.setEmail(request.getEmail());
        student.setBirthDate(request.getBirthDate());

        Student savedStudent = studentRepository.save(student);
        return studentMapper.toResponse(savedStudent);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student с id " + id + " не найден"));

        boolean isInActiveGroup = studyGroupRepository.existsByStudents_IdAndStatus(id, GroupStatus.ACTIVE);
        if (isInActiveGroup) {
            throw new ConflictException("Студент с id " + id + " состоит в активной учебной группе и не может быть удалён");
        }

        studentRepository.delete(student);
    }
}
