package com.covenantcode.crm.repository;

import com.covenantcode.crm.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;


public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    boolean existsByUser_Id(Long userId);

    boolean existsByIdAndStudyGroupsTeacherId(Long studentId, Long teacherId);

    Optional<Student> findByUser_Id(Long userId);
}
