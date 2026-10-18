package com.covenantcode.crm.service.impl;


import com.covenantcode.crm.dto.analytics.DashboardResponse;
import com.covenantcode.crm.entity.enums.CourseStatus;
import com.covenantcode.crm.entity.enums.GroupStatus;
import com.covenantcode.crm.entity.enums.LeadStatus;
import com.covenantcode.crm.repository.CourseRepository;
import com.covenantcode.crm.repository.LeadRepository;
import com.covenantcode.crm.repository.LessonRepository;
import com.covenantcode.crm.repository.StudentRepository;
import com.covenantcode.crm.repository.StudyGroupRepository;
import com.covenantcode.crm.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final LeadRepository leadRepository;
    private final StudentRepository studentRepository;
    private final LessonRepository lessonRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        // 1. Получить общее количество лидов
        long totalLeads = leadRepository.count();

        // 2. Собрать количество лидов по статусам
        Map<String, Long> leadsByStatus = new HashMap<>();
        for (LeadStatus status : LeadStatus.values()) {
            long count = leadRepository.countByStatus(status);
            leadsByStatus.put(status.name(), count);
        }

        // 3. Рассчитать conversionRate с округлением по ТЗ
        double conversionRate = 0.0;
        if (totalLeads > 0) {
            Long convertedCount = leadsByStatus.getOrDefault("CONVERTED_TO_STUDENT", 0L);
            double rawRate = (convertedCount * 100.0) / totalLeads;
            conversionRate = Math.round(rawRate * 10.0) / 10.0;
        }

        // 4. Получить количество студентов и курсов
        long totalStudents = studentRepository.count();
        long activeCourses = courseRepository.countByStatus(CourseStatus.ACTIVE);

        // 5. Получить счетчики групп по их статусам (из вашего StudyGroupRepository)
        long activeGroups = studyGroupRepository.countByStatus(GroupStatus.ACTIVE);
        long draftGroups = studyGroupRepository.countByStatus(GroupStatus.DRAFT);

        // 6. Расчет дат для занятий (сегодня и текущая неделя)
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        long upcomingLessonsToday = lessonRepository.countByLessonDate(today);
        long upcomingLessonsThisWeek = lessonRepository.countByLessonDateBetween(startOfWeek, endOfWeek);

        // 7. Сборка объекта ответа строго по вашим полям
        DashboardResponse response = new DashboardResponse();

        response.setTotalLeads(totalLeads);
        response.setLeadsByStatus(leadsByStatus);
        response.setConversionRate(conversionRate);
        response.setTotalStudents(totalStudents);
        response.setActiveCourses(activeCourses);
        response.setActiveGroups(activeGroups);
        response.setDraftGroups(draftGroups);
        response.setUpcomingLessonsToday(upcomingLessonsToday);
        response.setUpcomingLessonsThisWeek(upcomingLessonsThisWeek);

        return response;
    }
}
