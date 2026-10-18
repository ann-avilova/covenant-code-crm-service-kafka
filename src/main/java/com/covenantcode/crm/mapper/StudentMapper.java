package com.covenantcode.crm.mapper;
import com.covenantcode.crm.dto.student.StudentResponse;
import com.covenantcode.crm.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Mapper
public interface StudentMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(source = "user.id", target = "userId")
    StudentResponse toResponse(Student student);

    default LocalDateTime map(OffsetDateTime value) {
        return value != null ? value.toLocalDateTime() : null;
    }
}
