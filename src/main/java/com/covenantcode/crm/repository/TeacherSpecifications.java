package com.covenantcode.crm.repository;

import com.covenantcode.crm.entity.StudyGroup;
import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.enums.RoleName;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class TeacherSpecifications {

    private TeacherSpecifications() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<User> hasRole(RoleName role) {
        return (root, query, cb) -> {
            if (role == null) return cb.conjunction();
            return cb.equal(root.get("role").get("name"), role);
        };
    }

    public static Specification<User> searchByText(String text) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(text)) {
                return cb.conjunction();
            }

            String searchLower = "%" + text.trim().toLowerCase() + "%";

            Predicate firstName = cb.like(
                    cb.lower(root.get("firstName")),
                    searchLower
            );

            Predicate lastName = cb.like(
                    cb.lower(root.get("lastName")),
                    searchLower
            );

            Predicate email = cb.like(
                    cb.lower(root.get("email")),
                    searchLower
            );


            return cb.or(firstName, lastName, email);
        };
    }

    public static Specification<User> teachesSameCoursesAs(Long currentTeacherId) {
        return (root, query, cb) -> {
            if (currentTeacherId == null) {
                return cb.disjunction();
            }

            query.distinct(true);

            Subquery<Long> currentTeacherCourseIds = query.subquery(Long.class);
            Root<StudyGroup> currentTeacherGroup = currentTeacherCourseIds.from(StudyGroup.class);

            currentTeacherCourseIds.select(
                    currentTeacherGroup.get("course").get("id")
            ).where(
                    cb.equal(
                            currentTeacherGroup.get("teacher").get("id"),
                            currentTeacherId
                    )
            );

            Subquery<Long> candidateTeacherExists = query.subquery(Long.class);
            Root<StudyGroup> candidateTeacherGroup = candidateTeacherExists.from(StudyGroup.class);

            candidateTeacherExists.select(cb.literal(1L)).where(
                    cb.equal(
                            candidateTeacherGroup.get("teacher").get("id"),
                            root.get("id")
                    ),
                    candidateTeacherGroup.get("course").get("id").in(currentTeacherCourseIds)
            );

            return cb.exists(candidateTeacherExists);
        };
    }

    public static Specification<User> hasTeacherRole() {
        return (root, query, cb) ->
                cb.equal(root.get("role").get("name"), RoleName.TEACHER);
    }
}
