package com.task.manage.dashboard.api;

import com.task.manage.dashboard.dto.*;
import com.task.manage.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Dashboard Analytics and Metrics
 * Provides comprehensive endpoints for visualizing task management data
 *
 * Endpoints include:
 * - KPI metrics
 * - Status distribution and transitions
 * - Performance metrics
 * - Activity tracking
 * - Alerts and notifications
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Get overall KPI metrics for dashboard header
     * Use for: KPI cards, summary statistics
     *
     * Returns: Active tasks, completion rate, budget totals, etc.
     *
     * @param donorId Filter by donor ID
     * @param assignedPartnerId Filter by partner ID
     * @param fromDate Filter from date
     * @param toDate Filter to date
     */
    @GetMapping("/kpi")
    public ResponseEntity<DashboardKpiDto> getKpiMetrics(
            @RequestParam(required = false) Long donorId,
            @RequestParam(required = false) Long assignedPartnerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("GET /api/v1/dashboard/kpi - Fetching KPI metrics with filters: donorId={}, partnerId={}, fromDate={}, toDate={}",
                donorId, assignedPartnerId, fromDate, toDate);
        DashboardKpiDto kpi = dashboardService.getKpiMetrics(donorId, assignedPartnerId, fromDate, toDate);
        return ResponseEntity.ok(kpi);
    }

    /**
     * Get current status distribution
     * Use for: Donut chart, Pie chart, Bar chart
     * Chart: Donut chart showing task distribution by status
     *
     * @param donorId Filter by donor ID
     * @param assignedPartnerId Filter by partner ID
     * @param fromDate Filter from date
     * @param toDate Filter to date
     */
    @GetMapping("/status-distribution")
    public ResponseEntity<List<StatusDistributionDto>> getStatusDistribution(
            @RequestParam(required = false) Long donorId,
            @RequestParam(required = false) Long assignedPartnerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("GET /api/v1/dashboard/status-distribution - Fetching status distribution with filters");
        List<StatusDistributionDto> distribution = dashboardService.getStatusDistribution(donorId, assignedPartnerId, fromDate, toDate);
        return ResponseEntity.ok(distribution);
    }

    /**
     * Get average time spent in each status
     * Use for: Horizontal bar chart, Column chart
     * Chart: Horizontal bar chart with min/max range indicators
     *
     * @param donorId Filter by donor ID
     * @param assignedPartnerId Filter by partner ID
     * @param fromDate Filter from date
     * @param toDate Filter to date
     */
    @GetMapping("/status-duration")
    public ResponseEntity<List<StatusDurationDto>> getAverageTimePerStatus(
            @RequestParam(required = false) Long donorId,
            @RequestParam(required = false) Long assignedPartnerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("GET /api/v1/dashboard/status-duration - Fetching status duration metrics with filters");
        List<StatusDurationDto> durations = dashboardService.getAverageTimePerStatus(donorId, assignedPartnerId, fromDate, toDate);
        return ResponseEntity.ok(durations);
    }

    /**
     * Get tasks stuck in status
     * Use for: Alert cards, Table with urgency badges
     * Chart: Card list with color-coded urgency levels
     *
     * @param minHours Minimum hours in status to be considered stuck (default: 72)
     * @param donorId Filter by donor ID
     * @param assignedPartnerId Filter by partner ID
     * @param fromDate Filter from date
     * @param toDate Filter to date
     */
    @GetMapping("/stuck-tasks")
    public ResponseEntity<List<StuckTaskDto>> getStuckTasks(
            @RequestParam(required = false, defaultValue = "72") Integer minHours,
            @RequestParam(required = false) Long donorId,
            @RequestParam(required = false) Long assignedPartnerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("GET /api/v1/dashboard/stuck-tasks?minHours={} with filters", minHours);
        List<StuckTaskDto> stuckTasks = dashboardService.getStuckTasks(minHours, donorId, assignedPartnerId, fromDate, toDate);
        return ResponseEntity.ok(stuckTasks);
    }

    /**
     * Get status transition flow
     * Use for: Sankey diagram, Heatmap
     * Chart: Sankey diagram showing flow between statuses
     *
     * @param donorId Filter by donor ID
     * @param assignedPartnerId Filter by partner ID
     * @param fromDate Filter from date
     * @param toDate Filter to date
     */
    @GetMapping("/status-transitions")
    public ResponseEntity<List<StatusTransitionDto>> getStatusTransitions(
            @RequestParam(required = false) Long donorId,
            @RequestParam(required = false) Long assignedPartnerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("GET /api/v1/dashboard/status-transitions - Fetching transition flow with filters");
        List<StatusTransitionDto> transitions = dashboardService.getStatusTransitions(donorId, assignedPartnerId, fromDate, toDate);
        return ResponseEntity.ok(transitions);
    }

    /**
     * Get partner performance metrics
     * Use for: Table, Grouped bar chart, Heatmap
     * Chart: Table with sortable columns, completion rate badges
     *
     * @param donorId Filter by donor ID
     * @param fromDate Filter from date
     * @param toDate Filter to date
     */
    @GetMapping("/partner-performance")
    public ResponseEntity<List<PartnerPerformanceDto>> getPartnerPerformance(
            @RequestParam(required = false) Long donorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("GET /api/v1/dashboard/partner-performance - Fetching partner metrics with filters");
        List<PartnerPerformanceDto> performance = dashboardService.getPartnerPerformance(donorId, fromDate, toDate);
        return ResponseEntity.ok(performance);
    }

    /**
     * Get completion trend over time
     * Use for: Line chart, Combo chart (bar + line)
     * Chart: Combo chart with bars for count and line for average duration
     *
     * @param period Period for grouping: day, week, month (default: week)
     * @param donorId Filter by donor ID
     * @param assignedPartnerId Filter by partner ID
     * @param fromDate Filter from date
     * @param toDate Filter to date
     */
    @GetMapping("/completion-trend")
    public ResponseEntity<List<CompletionTrendDto>> getCompletionTrend(
            @RequestParam(required = false, defaultValue = "week") String period,
            @RequestParam(required = false) Long donorId,
            @RequestParam(required = false) Long assignedPartnerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("GET /api/v1/dashboard/completion-trend?period={} with filters", period);
        List<CompletionTrendDto> trend = dashboardService.getCompletionTrend(period, donorId, assignedPartnerId, fromDate, toDate);
        return ResponseEntity.ok(trend);
    }

    /**
     * Get status change activity over time
     * Use for: Stacked area chart, Multi-line chart
     * Chart: Stacked area chart showing activity by status
     *
     * @param days Number of days to look back (default: 30)
     * @param donorId Filter by donor ID
     * @param assignedPartnerId Filter by partner ID
     * @param fromDate Filter from date (overrides days if provided)
     * @param toDate Filter to date
     */
    @GetMapping("/status-activity")
    public ResponseEntity<List<StatusActivityDto>> getStatusActivity(
            @RequestParam(required = false, defaultValue = "30") Integer days,
            @RequestParam(required = false) Long donorId,
            @RequestParam(required = false) Long assignedPartnerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("GET /api/v1/dashboard/status-activity?days={} with filters", days);
        List<StatusActivityDto> activity = dashboardService.getStatusActivity(days, donorId, assignedPartnerId, fromDate, toDate);
        return ResponseEntity.ok(activity);
    }

    /**
     * Get user activity metrics
     * Use for: Grouped bar chart, Treemap, Table
     * Chart: Grouped bar chart showing changes per user by status
     *
     * @param days Number of days to look back (default: 30)
     * @param donorId Filter by donor ID
     * @param assignedPartnerId Filter by partner ID
     * @param fromDate Filter from date (overrides days if provided)
     * @param toDate Filter to date
     */
    @GetMapping("/user-activity")
    public ResponseEntity<List<UserActivityDto>> getUserActivity(
            @RequestParam(required = false, defaultValue = "30") Integer days,
            @RequestParam(required = false) Long donorId,
            @RequestParam(required = false) Long assignedPartnerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("GET /api/v1/dashboard/user-activity?days={} with filters", days);
        List<UserActivityDto> userActivity = dashboardService.getUserActivity(days, donorId, assignedPartnerId, fromDate, toDate);
        return ResponseEntity.ok(userActivity);
    }

    /**
     * Get activity calendar data
     * Use for: Calendar heatmap (GitHub-style)
     * Chart: Calendar heatmap with color intensity based on activity
     *
     * @param days Number of days to look back (default: 90)
     * @param donorId Filter by donor ID
     * @param assignedPartnerId Filter by partner ID
     * @param fromDate Filter from date (overrides days if provided)
     * @param toDate Filter to date
     */
    @GetMapping("/activity-calendar")
    public ResponseEntity<List<ActivityCalendarDto>> getActivityCalendar(
            @RequestParam(required = false, defaultValue = "90") Integer days,
            @RequestParam(required = false) Long donorId,
            @RequestParam(required = false) Long assignedPartnerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("GET /api/v1/dashboard/activity-calendar?days={} with filters", days);
        List<ActivityCalendarDto> calendar = dashboardService.getActivityCalendar(days, donorId, assignedPartnerId, fromDate, toDate);
        return ResponseEntity.ok(calendar);
    }

    /**
     * Get tasks approaching deadline
     * Use for: Alert cards, Table, Timeline
     * Chart: Card list with priority badges and deadline countdown
     *
     * @param days Days ahead to check for deadlines (default: 7)
     * @param donorId Filter by donor ID
     * @param assignedPartnerId Filter by partner ID
     */
    @GetMapping("/approaching-deadlines")
    public ResponseEntity<List<TaskDeadlineDto>> getTasksApproachingDeadline(
            @RequestParam(required = false, defaultValue = "7") Integer days,
            @RequestParam(required = false) Long donorId,
            @RequestParam(required = false) Long assignedPartnerId) {
        log.info("GET /api/v1/dashboard/approaching-deadlines?days={} with filters", days);
        List<TaskDeadlineDto> deadlines = dashboardService.getTasksApproachingDeadline(days, donorId, assignedPartnerId);
        return ResponseEntity.ok(deadlines);
    }

    /**
     * Get donor activity summary
     * Use for: Table, Card list
     * Chart: Table with sortable columns showing donor engagement
     *
     * @param fromDate Filter from date
     * @param toDate Filter to date
     */
    @GetMapping("/donor-activity")
    public ResponseEntity<List<DonorActivityDto>> getDonorActivity(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("GET /api/v1/dashboard/donor-activity - Fetching donor activity with filters");
        List<DonorActivityDto> donorActivity = dashboardService.getDonorActivity(fromDate, toDate);
        return ResponseEntity.ok(donorActivity);
    }

    /**
     * Get recent activity feed
     * Use for: Activity timeline, Feed list
     * Chart: Timeline showing recent status changes with user avatars
     *
     * @param limit Number of recent activities to return (default: 20)
     * @param donorId Filter by donor ID
     * @param assignedPartnerId Filter by partner ID
     */
    @GetMapping("/recent-activity")
    public ResponseEntity<List<RecentActivityDto>> getRecentActivity(
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestParam(required = false) Long donorId,
            @RequestParam(required = false) Long assignedPartnerId) {
        log.info("GET /api/v1/dashboard/recent-activity?limit={} with filters", limit);
        List<RecentActivityDto> recentActivity = dashboardService.getRecentActivity(limit, donorId, assignedPartnerId);
        return ResponseEntity.ok(recentActivity);
    }

    /**
     * Get workload distribution over time
     * Use for: Stacked area chart, Stacked bar chart
     * Chart: Stacked area showing task count by status over time
     *
     * @param days Number of days to look back (default: 30)
     * @param donorId Filter by donor ID
     * @param assignedPartnerId Filter by partner ID
     * @param fromDate Filter from date (overrides days if provided)
     * @param toDate Filter to date
     */
    @GetMapping("/workload-distribution")
    public ResponseEntity<List<WorkloadDistributionDto>> getWorkloadDistribution(
            @RequestParam(required = false, defaultValue = "30") Integer days,
            @RequestParam(required = false) Long donorId,
            @RequestParam(required = false) Long assignedPartnerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("GET /api/v1/dashboard/workload-distribution?days={} with filters", days);
        List<WorkloadDistributionDto> distribution = dashboardService.getWorkloadDistribution(days, donorId, assignedPartnerId, fromDate, toDate);
        return ResponseEntity.ok(distribution);
    }
}

