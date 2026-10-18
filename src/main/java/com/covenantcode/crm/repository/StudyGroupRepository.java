package com.covenantcode.crm.repository;

import com.covenantcode.crm.entity.StudyGroup;
import com.covenantcode.crm.entity.enums.GroupStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long>, JpaSpecificationExecutor<StudyGroup> {
    boolean existsByCourseIdAndStatus(Long courseId, GroupStatus status);

    boolean existsByStudents_IdAndStatus(Long studentId, GroupStatus status);

    long countByTeacherId(Long teacherId);

    long countByStatus(GroupStatus status);

}
