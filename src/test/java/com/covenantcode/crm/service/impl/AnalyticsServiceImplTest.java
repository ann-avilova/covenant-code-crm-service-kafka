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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private LeadRepository leadRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private StudyGroupRepository studyGroupRepository;
    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @BeforeEach
    void setUp() {
        // Базовая заглушка для статусов лидов, чтобы цикл по LeadStatus.values() не падал
        for (LeadStatus status : LeadStatus.values()) {
            when(leadRepository.countByStatus(status)).thenReturn(0L);
        }
    }

    @Test
    void getDashboard_ShouldCalculateConversionRateCorrectly_WhenLeadsExist() {
        when(leadRepository.count()).thenReturn(10L); // Заменено на count()
        when(leadRepository.countByStatus(LeadStatus.CONVERTED_TO_STUDENT)).thenReturn(3L);
        when(courseRepository.countByStatus(CourseStatus.ACTIVE)).thenReturn(0L);

        DashboardResponse response = analyticsService.getDashboard();

        assertEquals(30.0, response.getConversionRate());
    }

    @Test
    void getDashboard_ShouldReturnZeroConversionRate_WhenTotalLeadsIsZero() {
        when(leadRepository.count()).thenReturn(0L); // Заменено на count()
        when(courseRepository.countByStatus(CourseStatus.ACTIVE)).thenReturn(0L);

        DashboardResponse response = analyticsService.getDashboard();

        assertEquals(0.0, response.getConversionRate());
    }

    @Test
    void getDashboard_ShouldPopulateAllCountersCorrectly() {
        when(leadRepository.count()).thenReturn(5L); // Заменено на count()
        when(studentRepository.count()).thenReturn(15L);
        when(studyGroupRepository.countByStatus(GroupStatus.ACTIVE)).thenReturn(2L);
        when(studyGroupRepository.countByStatus(GroupStatus.DRAFT)).thenReturn(1L);
        when(lessonRepository.countByLessonDate(any(LocalDate.class))).thenReturn(4L);
        when(lessonRepository.countByLessonDateBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(20L);

        // When
        DashboardResponse response = analyticsService.getDashboard();

        // Then
        assertNotNull(response);
        assertEquals(5, response.getTotalLeads());
        assertNotNull(response.getLeadsByStatus());
        assertEquals(15, response.getTotalStudents());
        assertEquals(2, response.getActiveGroups());
        assertEquals(1, response.getDraftGroups());
        assertEquals(4, response.getUpcomingLessonsToday());
        assertEquals(20, response.getUpcomingLessonsThisWeek());
    }
}