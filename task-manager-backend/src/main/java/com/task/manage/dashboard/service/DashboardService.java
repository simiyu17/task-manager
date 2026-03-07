package com.task.manage.dashboard.service;

import com.task.manage.dashboard.dto.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for dashboard analytics and metrics with filtering support
 */
public interface DashboardService {

    /**
     * Get overall KPI metrics for dashboard header
     * @param donorId Filter by donor ID (optional)
     * @param assignedPartnerId Filter by partner ID (optional)
     * @param fromDate Filter from date (optional)
     * @param toDate Filter to date (optional)
     */
    DashboardKpiDto getKpiMetrics(Long donorId, Long assignedPartnerId, LocalDate fromDate, LocalDate toDate);

    /**
     * Get current status distribution for all active tasks
     */
    List<StatusDistributionDto> getStatusDistribution(Long donorId, Long assignedPartnerId, LocalDate fromDate, LocalDate toDate);

    /**
     * Get average time spent in each status
     */
    List<StatusDurationDto> getAverageTimePerStatus(Long donorId, Long assignedPartnerId, LocalDate fromDate, LocalDate toDate);

    /**
     * Get tasks stuck in status (> specified hours)
     */
    List<StuckTaskDto> getStuckTasks(Integer minHours, Long donorId, Long assignedPartnerId, LocalDate fromDate, LocalDate toDate);

    /**
     * Get status transition flow data (for Sankey diagram)
     */
    List<StatusTransitionDto> getStatusTransitions(Long donorId, Long assignedPartnerId, LocalDate fromDate, LocalDate toDate);

    /**
     * Get partner performance metrics
     */
    List<PartnerPerformanceDto> getPartnerPerformance(Long donorId, LocalDate fromDate, LocalDate toDate);

    /**
     * Get completion trend over time
     */
    List<CompletionTrendDto> getCompletionTrend(String period, Long donorId, Long assignedPartnerId, LocalDate fromDate, LocalDate toDate);

    /**
     * Get status change activity over time
     */
    List<StatusActivityDto> getStatusActivity(Integer days, Long donorId, Long assignedPartnerId, LocalDate fromDate, LocalDate toDate);

    /**
     * Get user activity metrics
     */
    List<UserActivityDto> getUserActivity(Integer days, Long donorId, Long assignedPartnerId, LocalDate fromDate, LocalDate toDate);

    /**
     * Get activity calendar data
     */
    List<ActivityCalendarDto> getActivityCalendar(Integer days, Long donorId, Long assignedPartnerId, LocalDate fromDate, LocalDate toDate);

    /**
     * Get tasks approaching deadline
     */
    List<TaskDeadlineDto> getTasksApproachingDeadline(Integer days, Long donorId, Long assignedPartnerId);

    /**
     * Get donor activity summary
     */
    List<DonorActivityDto> getDonorActivity(LocalDate fromDate, LocalDate toDate);

    /**
     * Get recent activity feed
     */
    List<RecentActivityDto> getRecentActivity(Integer limit, Long donorId, Long assignedPartnerId);

    /**
     * Get workload distribution over time
     */
    List<WorkloadDistributionDto> getWorkloadDistribution(Integer days, Long donorId, Long assignedPartnerId, LocalDate fromDate, LocalDate toDate);
}

