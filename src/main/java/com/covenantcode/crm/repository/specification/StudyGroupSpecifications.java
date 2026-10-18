package com.covenantcode.crm.repository.specification;

import com.covenantcode.crm.entity.StudyGroup;
import com.covenantcode.crm.entity.enums.GroupStatus;
import org.springframework.data.jpa.domain.Specification;


public final class StudyGroupSpecifications {

    private StudyGroupSpecifications() {
    }

    public static Specification<StudyGroup> withFilters(Long courseId, Long teacherId, GroupStatus status) {
        Specification<StudyGroup> specification = Specification.where(null);

        if (courseId != null) {
            specification = specification.and(hasCourseId(courseId));
        }

        if (teacherId != null) {
            specification = specification.and(hasTeacherId(teacherId));
        }

        if (status != null) {
            specification = specification.and(hasStatus(status));
        }

        return specification;
    }

    public static Specification<StudyGroup> hasCourseId(Long courseId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("course").get("id"), courseId);
    }

    public static Specification<StudyGroup> hasTeacherId(Long teacherId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("teacher").get("id"), teacherId);
    }

    public static Specification<StudyGroup> hasStatus(GroupStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }
}

