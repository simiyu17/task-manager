-- ========================================
-- DASHBOARD SQL QUERIES - Task Status History
-- ========================================

-- ----------------------------------------
-- 1. CURRENT STATUS OVERVIEW
-- ----------------------------------------
-- Shows current distribution of tasks across all statuses
-- Chart: Donut Chart or Bar Chart
SELECT
    task_status,
    COUNT(*) as task_count,
    ROUND(AVG(validated_budget), 2) as avg_budget,
    SUM(validated_budget) as total_budget
FROM tasks
WHERE data_status = 'ACTIVE'
GROUP BY task_status
ORDER BY task_count DESC;


-- ----------------------------------------
-- 2. AVERAGE TIME IN EACH STATUS
-- ----------------------------------------
-- Shows how long tasks typically spend in each status
-- Chart: Horizontal Bar Chart with Target Line
SELECT
    to_status,
    ROUND(AVG(duration_in_previous_status_hours), 2) as avg_hours,
    ROUND(MIN(duration_in_previous_status_hours), 2) as min_hours,
    ROUND(MAX(duration_in_previous_status_hours), 2) as max_hours,
    COUNT(*) as transition_count,
    PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY duration_in_previous_status_hours) as median_hours
FROM task_status_history
WHERE duration_in_previous_status_hours IS NOT NULL
GROUP BY to_status
ORDER BY avg_hours DESC;


-- ----------------------------------------
-- 3. STATUS TRANSITION FLOW (SANKEY DATA)
-- ----------------------------------------
-- Shows most common status transitions
-- Chart: Sankey Diagram or Heatmap
SELECT
    from_status as source,
    to_status as target,
    COUNT(*) as transition_count,
    ROUND(AVG(duration_in_previous_status_hours), 2) as avg_duration
FROM task_status_history
WHERE from_status IS NOT NULL
GROUP BY from_status, to_status
ORDER BY transition_count DESC;


-- ----------------------------------------
-- 4. TASKS STUCK IN STATUS (ALERT)
-- ----------------------------------------
-- Identifies tasks that haven't changed status in > 72 hours
-- Chart: Alert Card List with urgency indicators
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
        WHEN EXTRACT(HOUR FROM (CURRENT_TIMESTAMP - tsh.changed_at)) > 168 THEN 'CRITICAL'  -- > 7 days
        WHEN EXTRACT(HOUR FROM (CURRENT_TIMESTAMP - tsh.changed_at)) > 120 THEN 'HIGH'      -- > 5 days
        WHEN EXTRACT(HOUR FROM (CURRENT_TIMESTAMP - tsh.changed_at)) > 72 THEN 'MEDIUM'     -- > 3 days
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
  AND EXTRACT(HOUR FROM (CURRENT_TIMESTAMP - tsh.changed_at)) > 72
ORDER BY hours_in_status DESC
LIMIT 20;


-- ----------------------------------------
-- 5. TASK LIFECYCLE TIMELINE (INDIVIDUAL)
-- ----------------------------------------
-- Complete history of a single task
-- Chart: Gantt Chart / Timeline
SELECT
    tsh.id,
    tsh.from_status,
    tsh.to_status,
    tsh.changed_at,
    tsh.changed_by,
    tsh.duration_in_previous_status_hours,
    tsh.notes,
    LEAD(tsh.changed_at) OVER (ORDER BY tsh.changed_at) as next_change_time
FROM task_status_history tsh
WHERE tsh.task_id = ?  -- Parameter: specific task ID
ORDER BY tsh.changed_at ASC;


-- ----------------------------------------
-- 6. STATUS CHANGE ACTIVITY OVER TIME
-- ----------------------------------------
-- Shows volume of status changes per day
-- Chart: Stacked Area Chart or Multi-line Chart
SELECT
    DATE_TRUNC('day', changed_at)::date as change_date,
    to_status,
    COUNT(*) as changes_count
FROM task_status_history
WHERE changed_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY change_date, to_status
ORDER BY change_date, to_status;


-- ----------------------------------------
-- 7. USER ACTIVITY - STATUS CHANGES BY USER
-- ----------------------------------------
-- Shows who is most active in changing task statuses
-- Chart: Grouped Bar Chart or Treemap
SELECT
    changed_by,
    to_status,
    COUNT(*) as changes_made,
    MIN(changed_at) as first_change,
    MAX(changed_at) as last_change,
    ROUND(AVG(duration_in_previous_status_hours), 2) as avg_duration_before_change
FROM task_status_history
WHERE changed_at >= CURRENT_DATE - INTERVAL '30 days'
  AND changed_by IS NOT NULL
GROUP BY changed_by, to_status
ORDER BY changed_by, changes_made DESC;


-- ----------------------------------------
-- 8. COMPLETION RATE TREND
-- ----------------------------------------
-- Shows how many tasks completed per week and average time to complete
-- Chart: Combo Chart (Bar + Line)
SELECT
    DATE_TRUNC('week', tsh.changed_at)::date as week_start,
    COUNT(*) as completed_tasks,
    ROUND(AVG(
        (SELECT SUM(tsh2.duration_in_previous_status_hours)
         FROM task_status_history tsh2
         WHERE tsh2.task_id = tsh.task_id)
    ), 2) as avg_total_duration_hours,
    ROUND(AVG(
        (SELECT SUM(tsh2.duration_in_previous_status_hours)
         FROM task_status_history tsh2
         WHERE tsh2.task_id = tsh.task_id)
    ) / 24.0, 1) as avg_total_duration_days
FROM task_status_history tsh
WHERE tsh.to_status = 'COMPLETED'
  AND tsh.changed_at >= CURRENT_DATE - INTERVAL '6 months'
GROUP BY week_start
ORDER BY week_start;


-- ----------------------------------------
-- 9. SLOWEST STATUS TRANSITIONS
-- ----------------------------------------
-- Identifies tasks that took longest in each status
-- Chart: Table with duration bars
SELECT
    t.id,
    t.title,
    tsh.from_status,
    tsh.to_status,
    tsh.duration_in_previous_status_hours,
    tsh.changed_at,
    tsh.changed_by,
    p.partner_name,
    CASE
        WHEN tsh.duration_in_previous_status_hours > 168 THEN 'Very Slow'
        WHEN tsh.duration_in_previous_status_hours > 120 THEN 'Slow'
        WHEN tsh.duration_in_previous_status_hours > 72 THEN 'Moderate'
        ELSE 'Fast'
    END as speed_rating
FROM task_status_history tsh
JOIN tasks t ON tsh.task_id = t.id
LEFT JOIN partners p ON t.assigned_partner_id = p.id
WHERE tsh.duration_in_previous_status_hours IS NOT NULL
ORDER BY tsh.duration_in_previous_status_hours DESC
LIMIT 20;


-- ----------------------------------------
-- 10. PARTNER PERFORMANCE - STATUS EFFICIENCY
-- ----------------------------------------
-- Compares how quickly different partners move tasks through statuses
-- Chart: Heatmap or Grouped Bar Chart
SELECT
    p.partner_name,
    tsh.to_status,
    ROUND(AVG(tsh.duration_in_previous_status_hours), 2) as avg_hours,
    COUNT(*) as transitions,
    MIN(tsh.changed_at) as first_transition,
    MAX(tsh.changed_at) as last_transition
FROM task_status_history tsh
JOIN tasks t ON tsh.task_id = t.id
JOIN partners p ON t.assigned_partner_id = p.id
WHERE tsh.duration_in_previous_status_hours IS NOT NULL
  AND t.assigned_partner_id IS NOT NULL
GROUP BY p.partner_name, tsh.to_status
ORDER BY p.partner_name, avg_hours DESC;


-- ----------------------------------------
-- 11. STATUS PROGRESSION FUNNEL
-- ----------------------------------------
-- Shows how many tasks reached each major milestone
-- Chart: Funnel Chart
WITH status_reach AS (
    SELECT
        to_status,
        COUNT(DISTINCT task_id) as tasks_reached
    FROM task_status_history
    GROUP BY to_status
)
SELECT
    'INITIATED' as stage, 1 as stage_order,
    (SELECT COUNT(*) FROM tasks) as task_count
UNION ALL
SELECT 'ALLOCATED', 2, COALESCE((SELECT tasks_reached FROM status_reach WHERE to_status = 'ALLOCATED'), 0)
UNION ALL
SELECT 'ACCEPTED', 3, COALESCE((SELECT tasks_reached FROM status_reach WHERE to_status = 'ACCEPTED'), 0)
UNION ALL
SELECT 'WBS_SUBMITTED', 4, COALESCE((SELECT tasks_reached FROM status_reach WHERE to_status = 'WBS_SUBMITTED'), 0)
UNION ALL
SELECT 'CONCEPT_NOTE_APPROVED', 5, COALESCE((SELECT tasks_reached FROM status_reach WHERE to_status = 'CONCEPT_NOTE_APPROVED'), 0)
UNION ALL
SELECT 'INCEPTION_REPORT_APPROVED', 6, COALESCE((SELECT tasks_reached FROM status_reach WHERE to_status = 'INCEPTION_REPORT_APPROVED'), 0)
UNION ALL
SELECT 'EXECUTION_UNDERWAY', 7, COALESCE((SELECT tasks_reached FROM status_reach WHERE to_status = 'EXECUTION_UNDERWAY'), 0)
UNION ALL
SELECT 'COMPLETED', 8, COALESCE((SELECT tasks_reached FROM status_reach WHERE to_status = 'COMPLETED'), 0)
ORDER BY stage_order;


-- ----------------------------------------
-- 12. SLA COMPLIANCE TRACKING
-- ----------------------------------------
-- Tracks if status changes meet defined SLA targets
-- Chart: Gauge Chart or Progress Bars
WITH status_sla AS (
    SELECT 'TASK_UNDER_REVIEW' as status, 48 as sla_hours UNION ALL
    SELECT 'ALLOCATED', 24 UNION ALL
    SELECT 'CONCEPT_NOTE_UNDER_REVIEW', 72 UNION ALL
    SELECT 'INCEPTION_REPORT_UNDER_REVIEW', 72 UNION ALL
    SELECT 'EXECUTION_UNDERWAY', 720  -- 30 days
)
SELECT
    tsh.to_status,
    ss.sla_hours,
    ROUND(AVG(tsh.duration_in_previous_status_hours), 2) as avg_actual_hours,
    COUNT(*) as total_transitions,
    SUM(CASE WHEN tsh.duration_in_previous_status_hours <= ss.sla_hours THEN 1 ELSE 0 END) as within_sla,
    SUM(CASE WHEN tsh.duration_in_previous_status_hours > ss.sla_hours THEN 1 ELSE 0 END) as exceeded_sla,
    ROUND(
        100.0 * SUM(CASE WHEN tsh.duration_in_previous_status_hours <= ss.sla_hours THEN 1 ELSE 0 END) / COUNT(*),
        2
    ) as sla_compliance_percentage
FROM task_status_history tsh
JOIN status_sla ss ON tsh.to_status::text = ss.status
WHERE tsh.duration_in_previous_status_hours IS NOT NULL
GROUP BY tsh.to_status, ss.sla_hours
ORDER BY sla_compliance_percentage ASC;


-- ----------------------------------------
-- 13. STATUS CHANGE CALENDAR HEATMAP
-- ----------------------------------------
-- Shows activity patterns by date
-- Chart: Calendar Heatmap (GitHub-style)
SELECT
    DATE(changed_at) as change_date,
    COUNT(*) as changes_count,
    COUNT(DISTINCT task_id) as unique_tasks,
    COUNT(DISTINCT changed_by) as unique_users,
    ARRAY_AGG(DISTINCT changed_by) as active_users
FROM task_status_history
WHERE changed_at >= CURRENT_DATE - INTERVAL '90 days'
GROUP BY change_date
ORDER BY change_date;


-- ----------------------------------------
-- 14. BOTTLENECK ANALYSIS
-- ----------------------------------------
-- Identifies statuses where tasks get stuck the longest
-- Chart: Scatter Plot or Bubble Chart
SELECT
    t.id,
    t.title,
    tsh.to_status,
    tsh.duration_in_previous_status_hours,
    t.validated_budget,
    p.partner_name,
    d.donor_name,
    CASE
        WHEN tsh.duration_in_previous_status_hours > (
            SELECT AVG(duration_in_previous_status_hours) * 2
            FROM task_status_history
            WHERE to_status = tsh.to_status
        ) THEN 'OUTLIER'
        ELSE 'NORMAL'
    END as performance_flag
FROM task_status_history tsh
JOIN tasks t ON tsh.task_id = t.id
LEFT JOIN partners p ON t.assigned_partner_id = p.id
LEFT JOIN donors d ON t.donor_id = d.id
WHERE tsh.duration_in_previous_status_hours IS NOT NULL
  AND tsh.to_status IN ('TASK_UNDER_REVIEW', 'CONCEPT_NOTE_UNDER_REVIEW', 'INCEPTION_REPORT_UNDER_REVIEW')
ORDER BY tsh.duration_in_previous_status_hours DESC
LIMIT 50;


-- ----------------------------------------
-- 15. MONTHLY PERFORMANCE TREND
-- ----------------------------------------
-- Tracks performance metrics month over month
-- Chart: Multi-line Chart with Trend Lines
SELECT
    DATE_TRUNC('month', changed_at)::date as month,
    COUNT(DISTINCT task_id) as unique_tasks_changed,
    COUNT(*) as total_status_changes,
    ROUND(AVG(duration_in_previous_status_hours), 2) as avg_status_duration,
    COUNT(DISTINCT changed_by) as active_users,
    COUNT(*) FILTER (WHERE to_status = 'COMPLETED') as completed_count
FROM task_status_history
WHERE changed_at >= CURRENT_DATE - INTERVAL '12 months'
GROUP BY month
ORDER BY month;


-- ----------------------------------------
-- 16. PARTNER COMPARISON - TIME TO COMPLETE
-- ----------------------------------------
-- Compares total time from acceptance to completion by partner
-- Chart: Grouped Bar Chart
SELECT
    p.partner_name,
    COUNT(DISTINCT t.id) as completed_tasks,
    ROUND(AVG(
        SELECT SUM(tsh.duration_in_previous_status_hours)
        FROM task_status_history tsh
        WHERE tsh.task_id = t.id
          AND tsh.to_status >= 'ACCEPTED'
    ), 2) as avg_hours_to_complete,
    ROUND(MIN(
        SELECT SUM(tsh.duration_in_previous_status_hours)
        FROM task_status_history tsh
        WHERE tsh.task_id = t.id
          AND tsh.to_status >= 'ACCEPTED'
    ), 2) as fastest_completion,
    ROUND(MAX(
        SELECT SUM(tsh.duration_in_previous_status_hours)
        FROM task_status_history tsh
        WHERE tsh.task_id = t.id
          AND tsh.to_status >= 'ACCEPTED'
    ), 2) as slowest_completion
FROM tasks t
JOIN partners p ON t.assigned_partner_id = p.id
WHERE t.task_status = 'COMPLETED'
  AND t.assigned_partner_id IS NOT NULL
GROUP BY p.partner_name
ORDER BY avg_hours_to_complete ASC;


-- ----------------------------------------
-- 17. REAL-TIME STATUS BOARD DATA
-- ----------------------------------------
-- Current snapshot for Kanban board
-- Chart: Kanban Board
SELECT
    t.task_status,
    t.id,
    t.title,
    t.validated_budget,
    t.deadline,
    p.partner_name,
    d.donor_name,
    tsh.changed_at as current_status_since,
    EXTRACT(HOUR FROM (CURRENT_TIMESTAMP - tsh.changed_at)) as hours_in_current_status,
    tsh.changed_by as last_changed_by
FROM tasks t
LEFT JOIN (
    SELECT DISTINCT ON (task_id)
        task_id,
        changed_at,
        changed_by
    FROM task_status_history
    ORDER BY task_id, changed_at DESC
) tsh ON t.id = tsh.task_id
LEFT JOIN partners p ON t.assigned_partner_id = p.id
LEFT JOIN donors d ON t.donor_id = d.id
WHERE t.data_status = 'ACTIVE'
  AND t.task_status NOT IN ('COMPLETED', 'REJECTED')
ORDER BY t.task_status, tsh.changed_at DESC;


-- ----------------------------------------
-- 18. STATUS CHANGE FREQUENCY BY DAY OF WEEK
-- ----------------------------------------
-- Identifies which days see most activity
-- Chart: Polar Area Chart or Radial Bar Chart
SELECT
    TO_CHAR(changed_at, 'Day') as day_of_week,
    EXTRACT(DOW FROM changed_at) as day_number,
    COUNT(*) as changes_count,
    ROUND(AVG(duration_in_previous_status_hours), 2) as avg_duration
FROM task_status_history
WHERE changed_at >= CURRENT_DATE - INTERVAL '90 days'
GROUP BY day_of_week, day_number
ORDER BY day_number;


-- ----------------------------------------
-- 19. TASKS BY STATUS WITH HISTORY COUNT
-- ----------------------------------------
-- Shows current tasks with how many times they've changed status
-- Chart: Scatter Plot
SELECT
    t.id,
    t.title,
    t.task_status,
    COUNT(tsh.id) as status_changes_count,
    MIN(tsh.changed_at) as first_change,
    MAX(tsh.changed_at) as last_change,
    EXTRACT(DAY FROM (MAX(tsh.changed_at) - MIN(tsh.changed_at))) as task_age_days,
    p.partner_name
FROM tasks t
LEFT JOIN task_status_history tsh ON t.id = tsh.task_id
LEFT JOIN partners p ON t.assigned_partner_id = p.id
WHERE t.data_status = 'ACTIVE'
GROUP BY t.id, t.title, t.task_status, p.partner_name
ORDER BY status_changes_count DESC;


-- ----------------------------------------
-- 20. REJECTION ANALYSIS
-- ----------------------------------------
-- Analyzes tasks that were rejected and at what stage
-- Chart: Stacked Bar Chart
SELECT
    CASE
        WHEN to_status = 'REJECTED' THEN 'Initial Rejection'
        WHEN to_status = 'CONCEPT_NOTE_REJECTED' THEN 'Concept Note Stage'
        WHEN to_status = 'INCEPTION_REPORT_REJECTED' THEN 'Inception Report Stage'
    END as rejection_stage,
    COUNT(*) as rejection_count,
    ROUND(AVG(
        SELECT SUM(tsh2.duration_in_previous_status_hours)
        FROM task_status_history tsh2
        WHERE tsh2.task_id = tsh.task_id
          AND tsh2.changed_at <= tsh.changed_at
    ), 2) as avg_hours_before_rejection,
    ARRAY_AGG(tsh.notes) FILTER (WHERE tsh.notes IS NOT NULL) as rejection_reasons
FROM task_status_history tsh
WHERE to_status IN ('REJECTED', 'CONCEPT_NOTE_REJECTED', 'INCEPTION_REPORT_REJECTED')
GROUP BY rejection_stage
ORDER BY rejection_count DESC;


-- ----------------------------------------
-- 21. WORKLOAD DISTRIBUTION OVER TIME
-- ----------------------------------------
-- Shows how many tasks were in each status on each day
-- Chart: Stacked Area Chart
WITH date_series AS (
    SELECT generate_series(
        CURRENT_DATE - INTERVAL '30 days',
        CURRENT_DATE,
        INTERVAL '1 day'
    )::date as check_date
),
task_status_on_date AS (
    SELECT
        ds.check_date,
        t.id as task_id,
        (
            SELECT tsh.to_status
            FROM task_status_history tsh
            WHERE tsh.task_id = t.id
              AND tsh.changed_at::date <= ds.check_date
            ORDER BY tsh.changed_at DESC
            LIMIT 1
        ) as status_on_date
    FROM date_series ds
    CROSS JOIN tasks t
    WHERE t.date_created::date <= ds.check_date
)
SELECT
    check_date,
    status_on_date,
    COUNT(*) as task_count
FROM task_status_on_date
WHERE status_on_date IS NOT NULL
GROUP BY check_date, status_on_date
ORDER BY check_date, status_on_date;


-- ----------------------------------------
-- 22. DOCUMENT SUBMISSION TO APPROVAL TIME
-- ----------------------------------------
-- Time between document submission and approval for each document type
-- Chart: Box Plot or Violin Plot
SELECT
    CASE
        WHEN submitted.to_status = 'CONCEPT_NOTE_SUBMITTED' THEN 'CONCEPT_NOTE'
        WHEN submitted.to_status = 'INCEPTION_REPORT_SUBMITTED' THEN 'INCEPTION_REPORT'
    END as document_type,
    EXTRACT(HOUR FROM (approved.changed_at - submitted.changed_at)) as hours_to_approval,
    submitted.task_id,
    t.title
FROM task_status_history submitted
JOIN task_status_history approved ON submitted.task_id = approved.task_id
JOIN tasks t ON submitted.task_id = t.id
WHERE submitted.to_status IN ('CONCEPT_NOTE_SUBMITTED', 'INCEPTION_REPORT_SUBMITTED')
  AND approved.to_status IN ('CONCEPT_NOTE_APPROVED', 'INCEPTION_REPORT_APPROVED')
  AND approved.changed_at > submitted.changed_at
ORDER BY document_type, hours_to_approval;


-- ----------------------------------------
-- 23. DONOR REQUEST RESPONSE TIME
-- ----------------------------------------
-- Time from task creation to first meaningful action
-- Chart: Histogram with Target Line
SELECT
    d.donor_name,
    t.id,
    t.title,
    EXTRACT(HOUR FROM (first_action.changed_at - t.date_created)) as hours_to_first_action,
    first_action.to_status as first_action_status
FROM tasks t
JOIN donors d ON t.donor_id = d.id
JOIN (
    SELECT
        task_id,
        MIN(changed_at) as changed_at,
        (ARRAY_AGG(to_status ORDER BY changed_at))[1] as to_status
    FROM task_status_history
    WHERE to_status != 'INITIATED'
    GROUP BY task_id
) first_action ON t.id = first_action.task_id
ORDER BY hours_to_first_action DESC;


-- ----------------------------------------
-- 24. REVIEW CYCLE EFFICIENCY
-- ----------------------------------------
-- Compares time spent in different review stages
-- Chart: Grouped Column Chart
SELECT
    CASE
        WHEN to_status = 'TASK_UNDER_REVIEW' THEN 'Initial Review'
        WHEN to_status = 'CONCEPT_NOTE_UNDER_REVIEW' THEN 'Concept Note Review'
        WHEN to_status = 'INCEPTION_REPORT_UNDER_REVIEW' THEN 'Inception Report Review'
    END as review_stage,
    COUNT(*) as review_count,
    ROUND(AVG(duration_in_previous_status_hours), 2) as avg_review_hours,
    ROUND(MIN(duration_in_previous_status_hours), 2) as min_review_hours,
    ROUND(MAX(duration_in_previous_status_hours), 2) as max_review_hours,
    PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY duration_in_previous_status_hours) as median_hours,
    PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY duration_in_previous_status_hours) as p95_hours
FROM task_status_history
WHERE to_status IN ('TASK_UNDER_REVIEW', 'CONCEPT_NOTE_UNDER_REVIEW', 'INCEPTION_REPORT_UNDER_REVIEW')
  AND duration_in_previous_status_hours IS NOT NULL
GROUP BY review_stage
ORDER BY avg_review_hours DESC;


-- ----------------------------------------
-- 25. TOP PERFORMERS - FASTEST COMPLETIONS
-- ----------------------------------------
-- Lists fastest task completions
-- Chart: Leaderboard Table or Horizontal Bar
SELECT
    t.id,
    t.title,
    p.partner_name,
    d.donor_name,
    MIN(tsh.changed_at) as started_at,
    MAX(tsh.changed_at) FILTER (WHERE tsh.to_status = 'COMPLETED') as completed_at,
    EXTRACT(DAY FROM (
        MAX(tsh.changed_at) FILTER (WHERE tsh.to_status = 'COMPLETED') -
        MIN(tsh.changed_at)
    )) as days_to_complete,
    SUM(tsh.duration_in_previous_status_hours) as total_hours,
    COUNT(*) as status_changes
FROM tasks t
JOIN task_status_history tsh ON t.id = tsh.task_id
LEFT JOIN partners p ON t.assigned_partner_id = p.id
LEFT JOIN donors d ON t.donor_id = d.id
WHERE t.task_status = 'COMPLETED'
GROUP BY t.id, t.title, p.partner_name, d.donor_name
HAVING MAX(tsh.changed_at) FILTER (WHERE tsh.to_status = 'COMPLETED') IS NOT NULL
ORDER BY days_to_complete ASC
LIMIT 10;

