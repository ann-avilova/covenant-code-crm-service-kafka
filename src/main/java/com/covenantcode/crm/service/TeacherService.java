package com.covenantcode.crm.service;

import com.covenantcode.crm.dto.teacher.TeacherCreateRequest;
import com.covenantcode.crm.dto.teacher.TeacherResponse;
import com.covenantcode.crm.dto.teacher.TeacherUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TeacherService {
    Page<TeacherResponse> getAll(String search, Pageable pageable);


    TeacherResponse create(TeacherCreateRequest request);

    void delete(Long id);

    TeacherResponse getById(Long id);

    TeacherResponse update(Long id, TeacherUpdateRequest request);

    TeacherResponse setEnabled(Long id, boolean enabled);
}
