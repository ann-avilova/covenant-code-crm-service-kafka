package com.covenantcode.crm.mapper;

import com.covenantcode.crm.dto.group.CourseShortResponse;
import com.covenantcode.crm.dto.group.StudentShortResponse;
import com.covenantcode.crm.dto.group.StudyGroupResponse;
import com.covenantcode.crm.dto.group.UserShortResponse;
import com.covenantcode.crm.entity.Course;
import com.covenantcode.crm.entity.Student;
import com.covenantcode.crm.entity.StudyGroup;
import com.covenantcode.crm.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper
public interface StudyGroupMapper {

    StudyGroupResponse toResponse(StudyGroup group);

    @Mapping(source = "title", target = "name")
    CourseShortResponse toCourseShortResponse(Course course);

    UserShortResponse toUserShortResponse(User user);

    StudentShortResponse toStudentShortResponse(Student student);
}
