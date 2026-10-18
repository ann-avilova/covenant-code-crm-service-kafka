package com.covenantcode.crm.repository;

import com.covenantcode.crm.entity.Student;
import com.covenantcode.crm.entity.StudyGroup;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class StudentSpecifications {

    private StudentSpecifications() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<Student> searchByText(String search) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return cb.conjunction();
            }

            String searchLower = "%" + search.toLowerCase() + "%";

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

            Predicate phone = cb.like(
                    cb.lower(root.get("phone")),
                    searchLower
            );

            return cb.or(firstName, lastName, email, phone);
        };
    }

    public static Specification<Student> belongsToTeacherGroups(Long teacherId) {
        return (root, query, cb) -> {
            if (teacherId == null) {
                return null;
            }

            query.distinct(true);

            var subquery = query.subquery(Long.class);
            var groupRoot = subquery.from(StudyGroup.class);
            Join<StudyGroup, Student> studentsJoin = groupRoot.join("students");

            subquery.select(studentsJoin.get("id"))
                    .where(cb.equal(groupRoot.get("teacher").get("id"), teacherId));

            return root.get("id").in(subquery);
        };
    }

}
