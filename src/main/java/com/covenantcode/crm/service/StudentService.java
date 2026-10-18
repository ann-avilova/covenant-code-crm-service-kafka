package com.covenantcode.crm.service;

import com.covenantcode.crm.dto.student.StudentCreateRequest;
import com.covenantcode.crm.dto.student.StudentResponse;
import com.covenantcode.crm.dto.student.StudentUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface StudentService {

    StudentResponse getById(Long id);

    Page<StudentResponse> getAll(String search, Pageable pageable);

    StudentResponse update(Long id, StudentUpdateRequest request);

    StudentResponse create(StudentCreateRequest studentCreateRequest);

    void deleteById(Long id);

}
