package com.covenantcode.crm.mapper;

import com.covenantcode.crm.dto.attendance.AttendanceRecord;
import com.covenantcode.crm.dto.attendance.AttendanceResponse;
import com.covenantcode.crm.entity.Attendance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface AttendanceMapper {

    @Mapping(target = "lessonId", source = "lesson.id")
    @Mapping(target = "lessonDate", source = "lesson.lessonDate")
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentName", expression = "java(attendance.getStudent().getFirstName() + \" \" + attendance.getStudent().getLastName())")
    @Mapping(target = "markedAt", expression = "java(map(attendance.getMarkedAt()))")
    AttendanceResponse toResponse(Attendance attendance);

    List<AttendanceResponse> toResponseList(List<Attendance> attendances);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "markedAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateAttendanceFromRecord(AttendanceRecord record, @MappingTarget Attendance attendance);

    default LocalDateTime map(OffsetDateTime value) {
        return value != null ? value.toLocalDateTime() : null;
    }
}
