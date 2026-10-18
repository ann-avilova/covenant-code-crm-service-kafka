package com.covenantcode.crm.dto.group;

import com.covenantcode.crm.entity.enums.GroupStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGroupResponse {

    private Long id;
    private String name;
    private CourseShortResponse course;
    private UserShortResponse teacher;
    private List<StudentShortResponse> students;
    private LocalDate startDate;
    private GroupStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
