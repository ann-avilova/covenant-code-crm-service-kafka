package com.covenantcode.crm.dto.analytics;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private long totalLeads;
    private Map<String, Long> leadsByStatus;
    private double conversionRate;
    private long totalStudents;
    private long activeCourses;
    private long activeGroups;
    private long draftGroups;
    private long upcomingLessonsToday;
    private long upcomingLessonsThisWeek;
}
