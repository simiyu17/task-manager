package com.task.manage.dashboard.service;

import com.task.manage.dashboard.dto.*;
import com.task.manage.task.domain.Task;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final EntityManager entityManager;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private void setQueryParameters(Query query, Map<String, Object> parameters) {
        parameters.forEach(query::setParameter);
    }

    @Override
    public DashboardKpiDto getKpiMetrics(Long donorId, Long assignedPartnerId, LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching dashboard KPI metrics with filters: donorId={}, partnerId={}, fromDate={}, toDate={}",
                donorId, assignedPartnerId, fromDate, toDate);

        DashboardFilterBuilder filters = new DashboardFilterBuilder()
                .withDonorId(donorId)
                .withPartnerId(assignedPartnerId)
                .withFromDate(fromDate)
                .withToDate(toDate);

        String sql = """
            SELECT 
                COUNT(*) FILTER (WHERE t.task_status != 'COMPLETED') as total_active,
                COUNT(*) FILTER (WHERE t.task_status = 'COMPLETED') as total_completed,
                COUNT(*) FILTER (WHERE EXISTS (
                    SELECT 1 FROM task_status_history tsh
                    WHERE tsh.task_id = t.id
                    AND EXTRACT(HOUR FROM (CURRENT_TIMESTAMP - tsh.changed_at)) > 72
                    AND tsh.id = (SELECT MAX(tsh2.id) FROM task_status_history tsh2 WHERE tsh2.task_id = t.id)
                )) as stuck_tasks,
                COALESCE(AVG(
                    CASE WHEN t.task_status = 'COMPLETED' THEN
                        (SELECT SUM(tsh.duration_in_previous_status_hours)
                         FROM task_status_history tsh
                         WHERE tsh.task_id = t.id)
                    END
                ), 0) as avg_completion_hours,
                COALESCE(SUM(t.validated_budget) FILTER (WHERE t.task_status != 'COMPLETED'), 0) as total_active_budget,
                COUNT(*) FILTER (WHERE t.deadline IS NOT NULL 
                    AND t.deadline BETWEEN CURRENT_TIMESTAMP AND CURRENT_TIMESTAMP + INTERVAL '7 days'
                    AND t.task_status NOT IN ('COMPLETED', 'REJECTED')) as nearing_deadline,
                (SELECT COUNT(DISTINCT p.id) FROM partners p) as active_partners,
                (SELECT COUNT(DISTINCT d.id) FROM donors d) as active_donors,
                CASE 
                    WHEN COUNT(*) > 0 THEN ROUND(100.0 * COUNT(*) FILTER (WHERE t.task_status = 'COMPLETED') / COUNT(*), 2)
                    ELSE 0 
                END as completion_rate,
                COUNT(*) FILTER (WHERE DATE_TRUNC('month', t.date_created) = DATE_TRUNC('month', CURRENT_DATE)) as created_this_month,
                COUNT(*) FILTER (WHERE t.task_status = 'COMPLETED' 
                    AND EXISTS (
                        SELECT 1 FROM task_status_history tsh 
                        WHERE tsh.task_id = t.id 
                        AND tsh.to_status = 'COMPLETED'
                        AND DATE_TRUNC('month', tsh.changed_at) = DATE_TRUNC('month', CURRENT_DATE)
                    )) as completed_this_month
            FROM tasks t
            WHERE 1=1 """ + filters.buildWhereClause();

        Query query = entityManager.createNativeQuery(sql);
        setQueryParameters(query, filters.getParameters());
        Object[] result = (Object[]) query.getSingleResult();

        return new DashboardKpiDto(
                ((Number) result[0]).longValue(),
                ((Number) result[1]).longValue(),
                ((Number) result[2]).longValue(),
                ((Number) result[3]).doubleValue(),
                (BigDecimal) result[4],
                ((Number) result[5]).longValue(),
                ((Number) result[6]).longValue(),
                ((Number) result[7]).longValue(),
                ((Number) result[8]).doubleValue(),
                ((Number) result[9]).longValue(),
                ((Number) result[10]).longValue()
        );
    }

    @Override
    public List<StatusDistributionDto> getStatusDistribution(Long donorId, Long assignedPartnerId, LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching status distribution with filters");

        DashboardFilterBuilder filters = new DashboardFilterBuilder()
                .withDonorId(donorId)
                .withPartnerId(assignedPartnerId)
                .withFromDate(fromDate)
                .withToDate(toDate);

        String sql = """
            SELECT 
                task_status,
                COUNT(*) as task_count,
                ROUND(AVG(COALESCE(validated_budget, 0)), 2) as avg_budget
            FROM tasks t
            WHERE 1=1 """ + filters.buildWhereClause() + """
            
            GROUP BY task_status
            ORDER BY task_count DESC
            """;

        Query query = entityManager.createNativeQuery(sql);
        setQueryParameters(query, filters.getParameters());
        List<Object[]> results = query.getResultList();

        long totalTasks = results.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();

        return results.stream()
                .map(row -> {
                    String status = (String) row[0];
                    Long count = ((Number) row[1]).longValue();
                    Double avgBudget = ((Number) row[2]).doubleValue();
                    Double percentage = totalTasks > 0 ? (count * 100.0 / totalTasks) : 0.0;

                    Task.TaskStatus taskStatus = Task.TaskStatus.fromString(status);
                    String display = taskStatus != null ? taskStatus.getDisplayName() : status;

                    return new StatusDistributionDto(taskStatus, display, count, percentage, avgBudget);
                })
                .toList();
    }

    @Override
    public List<StatusDurationDto> getAverageTimePerStatus(Long donorId, Long assignedPartnerId, LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching average time per status with filters");

        // Build filter for task_status_history join
        StringBuilder filterClause = new StringBuilder(" WHERE tsh.duration_in_previous_status_hours IS NOT NULL");
        DashboardFilterBuilder filters = new DashboardFilterBuilder();

        if (donorId != null || assignedPartnerId != null || fromDate != null || toDate != null) {
            filterClause.append(" AND tsh.task_id IN (SELECT id FROM tasks t WHERE 1=1");
            filters.withDonorId(donorId)
                   .withPartnerId(assignedPartnerId)
                   .withFromDate(fromDate)
                   .withToDate(toDate);
            filterClause.append(filters.buildWhereClause()).append(")");
        }

        String sql = """
            SELECT 
                to_status,
                ROUND(AVG(duration_in_previous_status_hours), 2) as avg_hours,
                ROUND(MIN(duration_in_previous_status_hours), 2) as min_hours,
                ROUND(MAX(duration_in_previous_status_hours), 2) as max_hours,
                PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY duration_in_previous_status_hours) as median_hours,
                COUNT(*) as transition_count
            FROM task_status_history tsh
            """ + filterClause + """
            GROUP BY to_status
            ORDER BY avg_hours DESC
            """;

        Query query = entityManager.createNativeQuery(sql);
        setQueryParameters(query, filters.getParameters());
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> {
                    String status = (String) row[0];
                    Task.TaskStatus taskStatus = Task.TaskStatus.fromString(status);
                    String display = taskStatus != null ? taskStatus.getDisplayName() : status;

                    return new StatusDurationDto(
                            status,
                            display,
                            row[1] != null ? ((Number) row[1]).doubleValue() : 0.0,
                            row[2] != null ? ((Number) row[2]).doubleValue() : 0.0,
                            row[3] != null ? ((Number) row[3]).doubleValue() : 0.0,
                            row[4] != null ? ((Number) row[4]).doubleValue() : 0.0,
                            ((Number) row[5]).longValue()
                    );
                })
                .toList();
    }

    @Override
    public List<StuckTaskDto> getStuckTasks(Integer minHours, Long donorId, Long assignedPartnerId, LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching tasks stuck for more than {} hours with filters", minHours);

        if (minHours == null || minHours < 0) {
            minHours = 72;
        }

        DashboardFilterBuilder filters = new DashboardFilterBuilder()
                .withDonorId(donorId)
                .withPartnerId(assignedPartnerId)
                .withFromDate(fromDate)
                .withToDate(toDate);

        String sql = """
            SELECT 
                t.id,
                t.title,
                t.task_status as current_status,
                tsh.changed_at as status_since,
                EXTRACT(HOUR FROM (CURRENT_TIMESTAMP - tsh.changed_at)) as hours_in_status,
                tsh.changed_by,
                p.partner_name,
                d.donor_name,
                t.deadline,
                CASE 
                    WHEN EXTRACT(HOUR FROM (CURRENT_TIMESTAMP - tsh.changed_at)) > 168 THEN 'CRITICAL'
                    WHEN EXTRACT(HOUR FROM (CURRENT_TIMESTAMP - tsh.changed_at)) > 120 THEN 'HIGH'
                    WHEN EXTRACT(HOUR FROM (CURRENT_TIMESTAMP - tsh.changed_at)) > 72 THEN 'MEDIUM'
                    ELSE 'LOW'
                END as urgency_level
            FROM tasks t
            JOIN (
                SELECT DISTINCT ON (task_id) 
                    task_id, 
                    to_status, 
                    changed_at,
                    changed_by
                FROM task_status_history
                ORDER BY task_id, changed_at DESC
            ) tsh ON t.id = tsh.task_id
            LEFT JOIN partners p ON t.assigned_partner_id = p.id
            LEFT JOIN donors d ON t.donor_id = d.id
            WHERE t.task_status NOT IN ('COMPLETED', 'REJECTED')
              AND EXTRACT(HOUR FROM (CURRENT_TIMESTAMP - tsh.changed_at)) > :minHours
            """ + filters.buildWhereClause() + """
            ORDER BY hours_in_status DESC
            LIMIT 50
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("minHours", minHours);
        setQueryParameters(query, filters.getParameters());
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> {
                    String status = (String) row[2];
                    Task.TaskStatus taskStatus = Task.TaskStatus.fromString(status);
                    String display = taskStatus != null ? taskStatus.getDisplayName() : status;

                    return new StuckTaskDto(
                            ((Number) row[0]).longValue(),
                            (String) row[1],
                            status,
                            display,
                            row[3] != null ? ((Timestamp) row[3]).toLocalDateTime().format(DATETIME_FORMATTER) : null,
                            ((Number) row[4]).longValue(),
                            (String) row[9],
                            (String) row[6],
                            (String) row[7],
                            row[8] != null ? ((Timestamp) row[8]).toLocalDateTime().format(DATETIME_FORMATTER) : null,
                            (String) row[5]
                    );
                })
                .toList();
    }

    @Override
    public List<StatusTransitionDto> getStatusTransitions(Long donorId, Long assignedPartnerId, LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching status transition flow data with filters");

        StringBuilder filterClause = new StringBuilder(" WHERE tsh.from_status IS NOT NULL");
        DashboardFilterBuilder filters = new DashboardFilterBuilder();

        if (donorId != null || assignedPartnerId != null || fromDate != null || toDate != null) {
            filterClause.append(" AND tsh.task_id IN (SELECT id FROM tasks t WHERE 1=1");
            filters.withDonorId(donorId)
                   .withPartnerId(assignedPartnerId)
                   .withFromDate(fromDate)
                   .withToDate(toDate);
            filterClause.append(filters.buildWhereClause()).append(")");
        }

        String sql = """
            SELECT 
                from_status,
                to_status,
                COUNT(*) as transition_count,
                ROUND(AVG(duration_in_previous_status_hours), 2) as avg_duration
            FROM task_status_history tsh
            """ + filterClause + """
            GROUP BY from_status, to_status
            ORDER BY transition_count DESC
            """;

        Query query = entityManager.createNativeQuery(sql);
        setQueryParameters(query, filters.getParameters());
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> {
                    String fromStatus = (String) row[0];
                    String toStatus = (String) row[1];

                    Task.TaskStatus fromTaskStatus = Task.TaskStatus.fromString(fromStatus);
                    Task.TaskStatus toTaskStatus = Task.TaskStatus.fromString(toStatus);

                    String fromDisplay = fromTaskStatus != null ? fromTaskStatus.getDisplayName() : fromStatus;
                    String toDisplay = toTaskStatus != null ? toTaskStatus.getDisplayName() : toStatus;

                    return new StatusTransitionDto(
                            fromStatus,
                            fromDisplay,
                            toStatus,
                            toDisplay,
                            ((Number) row[2]).longValue(),
                            row[3] != null ? ((Number) row[3]).doubleValue() : 0.0
                    );
                })
                .toList();
    }

    @Override
    public List<PartnerPerformanceDto> getPartnerPerformance(Long donorId, LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching partner performance metrics with filters");

        DashboardFilterBuilder filters = new DashboardFilterBuilder()
                .withDonorId(donorId)
                .withFromDate(fromDate)
                .withToDate(toDate);

        String sql = """
            SELECT 
                p.id,
                p.partner_name,
                COUNT(t.id) as total_tasks,
                SUM(CASE WHEN t.task_status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_tasks,
                SUM(CASE WHEN t.task_status NOT IN ('COMPLETED', 'REJECTED') THEN 1 ELSE 0 END) as active_tasks,
                COALESCE(ROUND(AVG(
                    CASE WHEN t.task_status = 'COMPLETED' THEN
                        (SELECT SUM(tsh.duration_in_previous_status_hours)
                         FROM task_status_history tsh
                         WHERE tsh.task_id = t.id)
                    END
                ), 2), 0) as avg_completion_hours,
                CASE 
                    WHEN COUNT(t.id) > 0 THEN ROUND(100.0 * SUM(CASE WHEN t.task_status = 'COMPLETED' THEN 1 ELSE 0 END) / COUNT(t.id), 2)
                    ELSE 0 
                END as completion_rate,
                COALESCE(SUM(t.validated_budget), 0) as total_budget
            FROM partners p
            LEFT JOIN tasks t ON p.id = t.assigned_partner_id
            WHERE 1=1
            """ + (filters.hasFilters() ? " AND (t.id IS NULL OR (1=1" + filters.buildWhereClause() + "))" : "") + """
            GROUP BY p.id, p.partner_name
            ORDER BY total_tasks DESC
            """;

        Query query = entityManager.createNativeQuery(sql);
        setQueryParameters(query, filters.getParameters());
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> new PartnerPerformanceDto(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue(),
                        ((Number) row[4]).longValue(),
                        ((Number) row[5]).doubleValue(),
                        ((Number) row[6]).doubleValue(),
                        row[7] != null ? row[7].toString() : "0"
                ))
                .toList();
    }

    // Continued in next part...

    @Override
    public List<CompletionTrendDto> getCompletionTrend(String period, Long donorId, Long assignedPartnerId, LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching completion trend for period: {} with filters", period);

        String truncFunction = switch (period != null ? period.toLowerCase() : "week") {
            case "day" -> "day";
            case "month" -> "month";
            default -> "week";
        };

        StringBuilder filterClause = new StringBuilder(" WHERE tsh.to_status = 'COMPLETED'");
        DashboardFilterBuilder filters = new DashboardFilterBuilder();

        if (fromDate != null) {
            filterClause.append(" AND tsh.changed_at >= :fromDate");
            filters.getParameters().put("fromDate", fromDate.atStartOfDay());
        } else {
            filterClause.append(" AND tsh.changed_at >= CURRENT_DATE - INTERVAL '6 months'");
        }

        if (toDate != null) {
            filterClause.append(" AND tsh.changed_at <= :toDate");
            filters.getParameters().put("toDate", toDate.plusDays(1).atStartOfDay());
        }

        if (donorId != null || assignedPartnerId != null) {
            filterClause.append(" AND tsh.task_id IN (SELECT id FROM tasks t WHERE 1=1");
            DashboardFilterBuilder taskFilters = new DashboardFilterBuilder()
                    .withDonorId(donorId)
                    .withPartnerId(assignedPartnerId);
            filterClause.append(taskFilters.buildWhereClause()).append(")");
            filters.getParameters().putAll(taskFilters.getParameters());
        }

        String sql = """
            WITH first_completions AS (
                SELECT DISTINCT ON (tsh.task_id)
                    tsh.task_id,
                    tsh.changed_at,
                    (SELECT SUM(tsh2.duration_in_previous_status_hours)
                     FROM task_status_history tsh2
                     WHERE tsh2.task_id = tsh.task_id) as total_duration_hours
                FROM task_status_history tsh
            """.formatted(truncFunction) + filterClause + """
                ORDER BY tsh.task_id, tsh.changed_at ASC
            )
            SELECT 
                DATE_TRUNC('%s', fc.changed_at)::date as period_start,
                COUNT(DISTINCT fc.task_id) as completed_tasks,
                ROUND(AVG(fc.total_duration_hours), 2) as avg_total_duration_hours,
                ROUND(AVG(fc.total_duration_hours) / 24.0, 1) as avg_total_duration_days
            FROM first_completions fc
            GROUP BY period_start
            ORDER BY period_start
            """.formatted(truncFunction);

        Query query = entityManager.createNativeQuery(sql);
        setQueryParameters(query, filters.getParameters());
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> new CompletionTrendDto(
                        row[0] != null ? ((java.sql.Date) row[0]).toLocalDate() : LocalDate.now(),
                        ((Number) row[1]).longValue(),
                        row[2] != null ? ((Number) row[2]).doubleValue() : 0.0,
                        row[3] != null ? ((Number) row[3]).doubleValue() : 0.0
                ))
                .toList();
    }

    @Override
    public List<StatusActivityDto> getStatusActivity(Integer days, Long donorId, Long assignedPartnerId, LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching status activity for last {} days with filters", days);

        StringBuilder filterClause = new StringBuilder();
        DashboardFilterBuilder filters = new DashboardFilterBuilder();

        if (fromDate != null) {
            filterClause.append(" WHERE tsh.changed_at >= :fromDate");
            filters.getParameters().put("fromDate", fromDate.atStartOfDay());
        } else if (days != null && days > 0) {
            filterClause.append(" WHERE tsh.changed_at >= CURRENT_DATE - INTERVAL '").append(days).append(" days'");
        } else {
            filterClause.append(" WHERE tsh.changed_at >= CURRENT_DATE - INTERVAL '30 days'");
        }

        if (toDate != null) {
            filterClause.append(" AND tsh.changed_at <= :toDate");
            filters.getParameters().put("toDate", toDate.plusDays(1).atStartOfDay());
        }

        if (donorId != null || assignedPartnerId != null) {
            filterClause.append(" AND tsh.task_id IN (SELECT id FROM tasks t WHERE 1=1");
            DashboardFilterBuilder taskFilters = new DashboardFilterBuilder()
                    .withDonorId(donorId)
                    .withPartnerId(assignedPartnerId);
            filterClause.append(taskFilters.buildWhereClause()).append(")");
            filters.getParameters().putAll(taskFilters.getParameters());
        }

        String sql = """
            SELECT 
                DATE_TRUNC('day', tsh.changed_at)::date as change_date,
                tsh.to_status,
                COUNT(*) as changes_count
            FROM task_status_history tsh
            """ + filterClause + """
            GROUP BY change_date, tsh.to_status
            ORDER BY change_date, tsh.to_status
            """;

        Query query = entityManager.createNativeQuery(sql);
        setQueryParameters(query, filters.getParameters());
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> {
                    String status = (String) row[1];
                    Task.TaskStatus taskStatus = Task.TaskStatus.fromString(status);
                    String display = taskStatus != null ? taskStatus.getDisplayName() : status;

                    return new StatusActivityDto(
                            row[0] != null ? ((java.sql.Date) row[0]).toLocalDate() : LocalDate.now(),
                            status,
                            display,
                            ((Number) row[2]).longValue()
                    );
                })
                .toList();
    }

    @Override
    public List<UserActivityDto> getUserActivity(Integer days, Long donorId, Long assignedPartnerId, LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching user activity for last {} days with filters", days);

        StringBuilder filterClause = new StringBuilder(" WHERE tsh.changed_by IS NOT NULL");
        DashboardFilterBuilder filters = new DashboardFilterBuilder();

        if (fromDate != null) {
            filterClause.append(" AND tsh.changed_at >= :fromDate");
            filters.getParameters().put("fromDate", fromDate.atStartOfDay());
        } else if (days != null && days > 0) {
            filterClause.append(" AND tsh.changed_at >= CURRENT_DATE - INTERVAL '").append(days).append(" days'");
        } else {
            filterClause.append(" AND tsh.changed_at >= CURRENT_DATE - INTERVAL '30 days'");
        }

        if (toDate != null) {
            filterClause.append(" AND tsh.changed_at <= :toDate");
            filters.getParameters().put("toDate", toDate.plusDays(1).atStartOfDay());
        }

        if (donorId != null || assignedPartnerId != null) {
            filterClause.append(" AND tsh.task_id IN (SELECT id FROM tasks t WHERE 1=1");
            DashboardFilterBuilder taskFilters = new DashboardFilterBuilder()
                    .withDonorId(donorId)
                    .withPartnerId(assignedPartnerId);
            filterClause.append(taskFilters.buildWhereClause()).append(")");
            filters.getParameters().putAll(taskFilters.getParameters());
        }

        String sql = """
            SELECT 
                tsh.changed_by,
                tsh.to_status,
                COUNT(*) as changes_made,
                MIN(tsh.changed_at) as first_change,
                MAX(tsh.changed_at) as last_change
            FROM task_status_history tsh
            """ + filterClause + """
            GROUP BY tsh.changed_by, tsh.to_status
            ORDER BY tsh.changed_by, changes_made DESC
            """;

        Query query = entityManager.createNativeQuery(sql);
        setQueryParameters(query, filters.getParameters());
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> {
                    String status = (String) row[1];
                    Task.TaskStatus taskStatus = Task.TaskStatus.fromString(status);
                    String display = taskStatus != null ? taskStatus.getDisplayName() : status;

                    return new UserActivityDto(
                            (String) row[0],
                            status,
                            display,
                            ((Number) row[2]).longValue(),
                            row[3] != null ? ((Timestamp) row[3]).toLocalDateTime().format(DATETIME_FORMATTER) : null,
                            row[4] != null ? ((Timestamp) row[4]).toLocalDateTime().format(DATETIME_FORMATTER) : null
                    );
                })
                .toList();
    }

    @Override
    public List<ActivityCalendarDto> getActivityCalendar(Integer days, Long donorId, Long assignedPartnerId, LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching activity calendar for last {} days with filters", days);

        StringBuilder filterClause = new StringBuilder();
        DashboardFilterBuilder filters = new DashboardFilterBuilder();

        if (fromDate != null) {
            filterClause.append(" WHERE tsh.changed_at >= :fromDate");
            filters.getParameters().put("fromDate", fromDate.atStartOfDay());
        } else if (days != null && days > 0) {
            filterClause.append(" WHERE tsh.changed_at >= CURRENT_DATE - INTERVAL '").append(days).append(" days'");
        } else {
            filterClause.append(" WHERE tsh.changed_at >= CURRENT_DATE - INTERVAL '90 days'");
        }

        if (toDate != null) {
            filterClause.append(" AND tsh.changed_at <= :toDate");
            filters.getParameters().put("toDate", toDate.plusDays(1).atStartOfDay());
        }

        if (donorId != null || assignedPartnerId != null) {
            filterClause.append(" AND tsh.task_id IN (SELECT id FROM tasks t WHERE 1=1");
            DashboardFilterBuilder taskFilters = new DashboardFilterBuilder()
                    .withDonorId(donorId)
                    .withPartnerId(assignedPartnerId);
            filterClause.append(taskFilters.buildWhereClause()).append(")");
            filters.getParameters().putAll(taskFilters.getParameters());
        }

        String sql = """
            SELECT 
                DATE(tsh.changed_at) as change_date,
                COUNT(*) as changes_count,
                COUNT(DISTINCT tsh.task_id) as unique_tasks,
                COUNT(DISTINCT tsh.changed_by) as unique_users
            FROM task_status_history tsh
            """ + filterClause + """
            GROUP BY change_date
            ORDER BY change_date
            """;

        Query query = entityManager.createNativeQuery(sql);
        setQueryParameters(query, filters.getParameters());
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> new ActivityCalendarDto(
                        row[0] != null ? ((java.sql.Date) row[0]).toLocalDate() : LocalDate.now(),
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue()
                ))
                .toList();
    }

    @Override
    public List<TaskDeadlineDto> getTasksApproachingDeadline(Integer days, Long donorId, Long assignedPartnerId) {
        log.info("Fetching tasks approaching deadline within {} days with filters", days);

        if (days == null || days < 1) {
            days = 7;
        }

        DashboardFilterBuilder filters = new DashboardFilterBuilder()
                .withDonorId(donorId)
                .withPartnerId(assignedPartnerId);

        String sql = """
            SELECT 
                t.id,
                t.title,
                t.task_status,
                t.deadline,
                EXTRACT(DAY FROM (t.deadline - CURRENT_TIMESTAMP)) as days_remaining,
                p.partner_name,
                d.donor_name,
                CASE 
                    WHEN EXTRACT(DAY FROM (t.deadline - CURRENT_TIMESTAMP)) <= 1 THEN 'CRITICAL'
                    WHEN EXTRACT(DAY FROM (t.deadline - CURRENT_TIMESTAMP)) <= 3 THEN 'HIGH'
                    WHEN EXTRACT(DAY FROM (t.deadline - CURRENT_TIMESTAMP)) <= 7 THEN 'MEDIUM'
                    ELSE 'LOW'
                END as priority
            FROM tasks t
            JOIN partners p ON t.assigned_partner_id = p.id
            JOIN donors d ON t.donor_id = d.id
            WHERE t.deadline IS NOT NULL
              AND t.task_status NOT IN ('COMPLETED', 'REJECTED')
              AND t.deadline > CURRENT_TIMESTAMP
              AND t.deadline <= CURRENT_TIMESTAMP + INTERVAL ':days days'
            """ + filters.buildWhereClause() + """
            ORDER BY t.deadline ASC
            """;

        // Need to use string formatting for interval since PostgreSQL doesn't support parameters in intervals
        sql = sql.replace(":days", String.valueOf(days));

        Query query = entityManager.createNativeQuery(sql);
        setQueryParameters(query, filters.getParameters());
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> {
                    String status = (String) row[2];
                    Task.TaskStatus taskStatus = Task.TaskStatus.fromString(status);
                    String display = taskStatus != null ? taskStatus.getDisplayName() : status;

                    return new TaskDeadlineDto(
                            ((Number) row[0]).longValue(),
                            (String) row[1],
                            status,
                            display,
                            row[3] != null ? ((Timestamp) row[3]).toLocalDateTime().format(DATETIME_FORMATTER) : null,
                            row[4] != null ? ((Number) row[4]).longValue() : 0L,
                            (String) row[5],
                            (String) row[6],
                            (String) row[7]
                    );
                })
                .toList();
    }

    @Override
    public List<DonorActivityDto> getDonorActivity(LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching donor activity summary with filters");

        StringBuilder filterClause = new StringBuilder();
        DashboardFilterBuilder filters = new DashboardFilterBuilder();

        if (fromDate != null || toDate != null) {
            filterClause.append(" AND (t.id IS NULL OR (1=1");
            filters.withFromDate(fromDate).withToDate(toDate);
            filterClause.append(filters.buildWhereClause()).append("))");
        }

        String sql = """
            SELECT 
                d.id,
                d.donor_name,
                d.email_address,
                COUNT(t.id) as total_requests,
                SUM(CASE WHEN t.task_status NOT IN ('COMPLETED', 'REJECTED') THEN 1 ELSE 0 END) as active_requests,
                SUM(CASE WHEN t.task_status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_requests,
                COALESCE(SUM(t.validated_budget), 0) as total_budget,
                MAX(t.request_received_at) as last_request_date
            FROM donors d
            LEFT JOIN tasks t ON d.id = t.donor_id
            WHERE 1=1
            """ + filterClause + """
            GROUP BY d.id, d.donor_name, d.email_address
            ORDER BY total_requests DESC
            """;

        Query query = entityManager.createNativeQuery(sql);
        setQueryParameters(query, filters.getParameters());
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> new DonorActivityDto(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        ((Number) row[3]).longValue(),
                        ((Number) row[4]).longValue(),
                        ((Number) row[5]).longValue(),
                        row[6] != null ? row[6].toString() : "0",
                        row[7] != null ? ((Timestamp) row[7]).toLocalDateTime().format(DATETIME_FORMATTER) : null
                ))
                .toList();
    }

    @Override
    public List<RecentActivityDto> getRecentActivity(Integer limit, Long donorId, Long assignedPartnerId) {
        log.info("Fetching recent activity, limit: {} with filters", limit);

        if (limit == null || limit < 1) {
            limit = 20;
        }

        StringBuilder filterClause = new StringBuilder();
        DashboardFilterBuilder filters = new DashboardFilterBuilder();

        if (donorId != null || assignedPartnerId != null) {
            filterClause.append(" AND tsh.task_id IN (SELECT id FROM tasks t WHERE 1=1");
            filters.withDonorId(donorId).withPartnerId(assignedPartnerId);
            filterClause.append(filters.buildWhereClause()).append(")");
        }

        String sql = """
            SELECT 
                tsh.id,
                t.id,
                t.title,
                tsh.from_status,
                tsh.to_status,
                tsh.changed_at,
                tsh.changed_by,
                tsh.duration_in_previous_status_hours
            FROM task_status_history tsh
            JOIN tasks t ON tsh.task_id = t.id
            WHERE 1=1
            """ + filterClause + """
            ORDER BY tsh.changed_at DESC
            LIMIT :limit
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("limit", limit);
        setQueryParameters(query, filters.getParameters());
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> {
                    String fromStatus = (String) row[3];
                    String toStatus = (String) row[4];

                    Task.TaskStatus fromTaskStatus = fromStatus != null ? Task.TaskStatus.fromString(fromStatus) : null;
                    Task.TaskStatus toTaskStatus = Task.TaskStatus.fromString(toStatus);

                    String fromDisplay = fromTaskStatus != null ? fromTaskStatus.getDisplayName() : fromStatus;
                    String toDisplay = toTaskStatus != null ? toTaskStatus.getDisplayName() : toStatus;

                    return new RecentActivityDto(
                            ((Number) row[0]).longValue(),
                            ((Number) row[1]).longValue(),
                            (String) row[2],
                            fromStatus,
                            fromDisplay,
                            toStatus,
                            toDisplay,
                            row[5] != null ? ((Timestamp) row[5]).toLocalDateTime().format(DATETIME_FORMATTER) : null,
                            (String) row[6],
                            row[7] != null ? ((Number) row[7]).longValue() : null
                    );
                })
                .toList();
    }

    @Override
    public List<WorkloadDistributionDto> getWorkloadDistribution(Integer days, Long donorId, Long assignedPartnerId, LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching workload distribution for last {} days with filters", days);

        StringBuilder filterClause = new StringBuilder();
        DashboardFilterBuilder filters = new DashboardFilterBuilder();

        if (fromDate != null) {
            filterClause.append(" WHERE tsh.changed_at >= :fromDate");
            filters.getParameters().put("fromDate", fromDate.atStartOfDay());
        } else if (days != null && days > 0) {
            filterClause.append(" WHERE tsh.changed_at >= CURRENT_DATE - INTERVAL '").append(days).append(" days'");
        } else {
            filterClause.append(" WHERE tsh.changed_at >= CURRENT_DATE - INTERVAL '30 days'");
        }

        if (toDate != null) {
            filterClause.append(" AND tsh.changed_at <= :toDate");
            filters.getParameters().put("toDate", toDate.plusDays(1).atStartOfDay());
        }

        if (donorId != null || assignedPartnerId != null) {
            filterClause.append(" AND tsh.task_id IN (SELECT id FROM tasks t WHERE 1=1");
            DashboardFilterBuilder taskFilters = new DashboardFilterBuilder()
                    .withDonorId(donorId)
                    .withPartnerId(assignedPartnerId);
            filterClause.append(taskFilters.buildWhereClause()).append(")");
            filters.getParameters().putAll(taskFilters.getParameters());
        }

        String sql = """
            SELECT 
                DATE_TRUNC('day', tsh.changed_at)::date as change_date,
                tsh.to_status,
                COUNT(DISTINCT tsh.task_id) as task_count
            FROM task_status_history tsh
            """ + filterClause + """
            GROUP BY change_date, tsh.to_status
            ORDER BY change_date, tsh.to_status
            """;

        Query query = entityManager.createNativeQuery(sql);
        setQueryParameters(query, filters.getParameters());
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> {
                    String status = (String) row[1];
                    Task.TaskStatus taskStatus = Task.TaskStatus.fromString(status);
                    String display = taskStatus != null ? taskStatus.getDisplayName() : status;

                    return new WorkloadDistributionDto(
                            row[0] != null ? ((java.sql.Date) row[0]).toString() : LocalDate.now().toString(),
                            status,
                            display,
                            ((Number) row[2]).longValue()
                    );
                })
                .toList();
    }
}
