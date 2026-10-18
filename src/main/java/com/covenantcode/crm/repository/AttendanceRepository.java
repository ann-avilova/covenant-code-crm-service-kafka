package com.covenantcode.crm.repository;

import com.covenantcode.crm.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findAllByLessonId(Long lessonId);

    Page<Attendance> findAllByStudentId(Long studentId, Pageable pageable);

    Optional<Attendance> findByLessonIdAndStudentId(Long lessonId, Long studentId);
}

