# Dashboard Components & Chart Recommendations

## Task Status History Analytics

### 1. Task Lifecycle Timeline (Per Task Detail View)
**SQL Query:**
```sql
SELECT 
    tsh.from_status,
    tsh.to_status,
    tsh.changed_at,
    tsh.changed_by,
    tsh.duration_in_previous_status_hours,
    tsh.notes
FROM task_status_history tsh
WHERE tsh.task_id = ?
ORDER BY tsh.changed_at ASC;
```

**Chart Type:** Gantt Chart / Timeline Visualization
- **X-axis:** Time
- **Y-axis:** Status names
- **Bar width:** Duration in that status
- **Color coding:** Different colors for each status type
- **Hover details:** Show who changed it, when, and notes
- **Interactive:** Click to see full details of the transition

**UI Library Suggestions:**
- `chart.js` with `chartjs-chart-timeline` plugin
- `ApexCharts` Timeline
- `react-gantt-chart`
- `vis-timeline`

---

### 2. Average Time in Each Status (Overview Dashboard)
**SQL Query:**
```sql
SELECT 
    to_status,
    AVG(duration_in_previous_status_hours) as avg_hours,
    MIN(duration_in_previous_status_hours) as min_hours,
    MAX(duration_in_previous_status_hours) as max_hours,
    COUNT(*) as transition_count
FROM task_status_history
WHERE from_status IS NOT NULL 
  AND duration_in_previous_status_hours IS NOT NULL
GROUP BY to_status
ORDER BY avg_hours DESC;
```

**Chart Type:** Horizontal Bar Chart with Target Lines
- **Y-axis:** Status names (sorted by average duration)
- **X-axis:** Hours
- **Bars:** Average duration with min/max range indicators
- **Target line:** SLA or expected duration (if defined)
- **Color coding:** Green (within target), Yellow (close to target), Red (over target)

**Alternative:** Box Plot Chart
- Shows distribution of time spent in each status
- Visualizes outliers and quartiles

---

### 3. Status Transition Heatmap
**SQL Query:**
```sql
SELECT 
    from_status,
    to_status,
    COUNT(*) as transition_count
FROM task_status_history
WHERE from_status IS NOT NULL
GROUP BY from_status, to_status
ORDER BY transition_count DESC;
```

**Chart Type:** Heatmap or Sankey Diagram
- **Heatmap:** 
  - Rows: From Status
  - Columns: To Status
  - Color intensity: Number of transitions
- **Sankey Diagram:**
  - Shows flow between statuses
  - Width of connection = frequency
  - Helps identify common workflow paths

**Use Case:** Identify bottlenecks and unusual transitions

---

### 4. Status Change Activity Over Time
**SQL Query:**
```sql
SELECT 
    DATE_TRUNC('day', changed_at) as change_date,
    to_status,
    COUNT(*) as changes_count
FROM task_status_history
WHERE changed_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY change_date, to_status
ORDER BY change_date, to_status;
```

**Chart Type:** Stacked Area Chart or Grouped Bar Chart
- **X-axis:** Date
- **Y-axis:** Number of status changes
- **Series:** Each status as a separate line/area/bar group
- **Interactive:** Toggle status visibility
- **Date filter:** Last 7 days, 30 days, 90 days, custom range

**Alternative:** Multi-line Line Chart
- Easier to see individual trends per status

---

### 5. User Activity - Status Changes by User
**SQL Query:**
```sql
SELECT 
    changed_by,
    to_status,
    COUNT(*) as changes_made,
    MIN(changed_at) as first_change,
    MAX(changed_at) as last_change
FROM task_status_history
WHERE changed_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY changed_by, to_status
ORDER BY changed_by, changes_made DESC;
```

**Chart Type:** Grouped Bar Chart or Treemap
- **Grouped Bar Chart:**
  - X-axis: User names
  - Y-axis: Number of changes
  - Groups: Different statuses (different colors)
  
- **Treemap:**
  - Hierarchical view: User > Status > Count
  - Size of rectangle = number of changes
  - Color = user or status category

**Use Case:** Track team activity and workload distribution

---

### 6. Tasks Stuck in Status (Alert Dashboard)
**SQL Query:**
```sql
SELECT 
    t.id,
    t.title,
    t.task_status as current_status,
    tsh.changed_at as status_since,
    EXTRACT(HOUR FROM (CURRENT_TIMESTAMP - tsh.changed_at)) as hours_in_status,
    p.partner_name,
    d.donor_name
FROM tasks t
JOIN (
    SELECT DISTINCT ON (task_id) 
        task_id, 
        to_status, 
        changed_at
    FROM task_status_history
    ORDER BY task_id, changed_at DESC
) tsh ON t.id = tsh.task_id
LEFT JOIN partners p ON t.assigned_partner_id = p.id
LEFT JOIN donors d ON t.donor_id = d.id
WHERE EXTRACT(HOUR FROM (CURRENT_TIMESTAMP - tsh.changed_at)) > 72  -- Stuck for > 3 days
  AND t.task_status NOT IN ('COMPLETED', 'REJECTED')
ORDER BY hours_in_status DESC;
```

**Chart Type:** Alert Card List with Progress Bars
- **Card layout:**
  - Task title (bold)
  - Current status badge
  - Time in status with progress bar
  - Assignee information
  - Visual alert level (red/yellow)
- **Sort options:** By duration, by priority, by assignee

**Alternative:** Bubble Chart
- X-axis: Time in status
- Y-axis: Status type
- Bubble size: Task budget
- Color: Alert level

---

### 7. Status Progression Funnel
**SQL Query:**
```sql
WITH status_counts AS (
    SELECT 
        to_status,
        COUNT(DISTINCT task_id) as task_count
    FROM task_status_history
    GROUP BY to_status
)
SELECT 
    ts.step_value,
    ts.status_name,
    COALESCE(sc.task_count, 0) as task_count
FROM (
    VALUES 
        (1, 'INITIATED'),
        (2, 'TASK_UNDER_REVIEW'),
        (3, 'ALLOCATED'),
        (4, 'ACCEPTED'),
        (6, 'WBS_SUBMITTED'),
        (7, 'CONCEPT_NOTE_SUBMITTED'),
        (15, 'EXECUTION_UNDERWAY'),
        (16, 'COMPLETED')
) ts(step_value, status_name)
LEFT JOIN status_counts sc ON sc.to_status::text = ts.status_name
ORDER BY ts.step_value;
```

**Chart Type:** Funnel Chart
- Shows drop-off at each major workflow stage
- Width represents number of tasks
- Percentage labels show completion rate
- Color gradient from start to finish

**Use Case:** Visualize workflow efficiency and identify where tasks drop off

---

### 8. Status Duration Distribution
**SQL Query:**
```sql
SELECT 
    to_status,
    duration_in_previous_status_hours,
    COUNT(*) as frequency
FROM task_status_history
WHERE duration_in_previous_status_hours IS NOT NULL
GROUP BY to_status, duration_in_previous_status_hours
ORDER BY to_status, duration_in_previous_status_hours;
```

**Chart Type:** Violin Plot or Histogram
- **Violin Plot:**
  - Shows distribution density of durations per status
  - Identifies typical vs outlier durations
  
- **Histogram:**
  - Bins: Duration ranges (0-24h, 24-48h, 48-72h, >72h)
  - Bars: Count of transitions in each bin
  - Faceted by status type

**Use Case:** Understand variability and set realistic SLAs

---

### 9. Real-Time Status Board
**SQL Query:**
```sql
SELECT 
    t.task_status,
    COUNT(t.id) as task_count,
    AVG(EXTRACT(HOUR FROM (CURRENT_TIMESTAMP - tsh.changed_at))) as avg_hours_in_status,
    ARRAY_AGG(t.title) FILTER (WHERE t.task_status NOT IN ('COMPLETED', 'REJECTED')) as active_tasks
FROM tasks t
LEFT JOIN (
    SELECT DISTINCT ON (task_id) 
        task_id, 
        changed_at
    FROM task_status_history
    ORDER BY task_id, changed_at DESC
) tsh ON t.id = tsh.task_id
WHERE t.data_status = 'ACTIVE'
GROUP BY t.task_status
ORDER BY t.task_status;
```

**Chart Type:** Kanban Board / Card Grid
- **Columns:** Each task status
- **Cards:** Individual tasks
- **Card info:**
  - Task title
  - Time in current status
  - Assignee
  - Priority indicator
- **Drag & drop:** To change status
- **Real-time updates:** WebSocket or polling

---

### 10. Completion Rate Trend
**SQL Query:**
```sql
SELECT 
    DATE_TRUNC('week', changed_at) as week,
    COUNT(*) as completed_tasks,
    AVG(
        SELECT SUM(duration_in_previous_status_hours)
        FROM task_status_history tsh2
        WHERE tsh2.task_id = tsh.task_id
    ) as avg_total_duration
FROM task_status_history tsh
WHERE to_status = 'COMPLETED'
  AND changed_at >= CURRENT_DATE - INTERVAL '6 months'
GROUP BY week
ORDER BY week;
```

**Chart Type:** Combo Chart (Line + Bar)
- **Bars:** Number of completed tasks per week
- **Line:** Average total duration to completion
- **Trend line:** Polynomial regression to show improvement
- **Color coding:** Green bars for weeks meeting target

**Use Case:** Track productivity improvements over time

---

### 11. Partner Performance - Status Change Efficiency
**SQL Query:**
```sql
SELECT 
    p.partner_name,
    tsh.to_status,
    AVG(tsh.duration_in_previous_status_hours) as avg_duration,
    COUNT(*) as transitions
FROM task_status_history tsh
JOIN tasks t ON tsh.task_id = t.id
JOIN partners p ON t.assigned_partner_id = p.id
WHERE tsh.duration_in_previous_status_hours IS NOT NULL
  AND t.assigned_partner_id IS NOT NULL
GROUP BY p.partner_name, tsh.to_status
ORDER BY p.partner_name, tsh.to_status;
```

**Chart Type:** Grouped Bar Chart or Heatmap
- **Grouped Bar:**
  - X-axis: Partner names
  - Y-axis: Average hours
  - Groups: Different statuses
  
- **Heatmap:**
  - Rows: Partners
  - Columns: Statuses
  - Color: Duration (green=fast, red=slow)

**Use Case:** Compare partner efficiency across workflow stages

---

### 12. Status Change Frequency Calendar
**SQL Query:**
```sql
SELECT 
    DATE(changed_at) as change_date,
    COUNT(*) as changes_count,
    STRING_AGG(DISTINCT changed_by, ', ') as users_active
FROM task_status_history
WHERE changed_at >= CURRENT_DATE - INTERVAL '3 months'
GROUP BY change_date
ORDER BY change_date;
```

**Chart Type:** Calendar Heatmap
- **Grid:** Calendar view (GitHub contribution style)
- **Color intensity:** Number of status changes that day
- **Hover:** Show count and active users
- **Click:** Drill down to see specific changes

**Use Case:** Identify busy periods and activity patterns

---

## Dashboard Layout Recommendations

### Executive Dashboard (High-Level Overview)
1. **Top Row:** KPI Cards
   - Total active tasks
   - Average completion time
   - Tasks stuck > 72 hours (red alert)
   - Completion rate this month

2. **Middle Row:**
   - Status Overview (Donut Chart) - Current distribution
   - Status Transition Flow (Sankey Diagram)
   - Completion Trend (Combo Chart)

3. **Bottom Row:**
   - Tasks Approaching Deadline (Timeline)
   - Partner Performance Summary (Grouped Bar)

### Operations Dashboard (Detail View)
1. **Left Panel:** Real-Time Status Board (Kanban)
2. **Right Panel:**
   - Tasks Stuck in Status (Alert Cards)
   - Recent Status Changes (Activity Feed)
   - Status Duration Distribution (Box Plot)

### Analytics Dashboard (Deep Dive)
1. **Status Change Activity Over Time** (Stacked Area)
2. **Average Time in Each Status** (Horizontal Bar with SLA)
3. **Status Change Frequency Calendar** (Heatmap)
4. **User Activity** (Treemap or Grouped Bar)

### Task Detail View
1. **Task Lifecycle Timeline** (Gantt Chart)
2. **Status History Table** (Data Grid with sorting/filtering)
3. **Duration Breakdown** (Pie Chart showing time in each status)

---

## UI/UX Best Practices

### Color Coding Standards
- **INITIATED / TASK_UNDER_REVIEW:** Blue (#2196F3)
- **ALLOCATED / ACCEPTED:** Green (#4CAF50)
- **REJECTED:** Red (#F44336)
- **WBS_SUBMITTED:** Purple (#9C27B0)
- **CONCEPT_NOTE stages:** Orange (#FF9800)
- **INCEPTION_REPORT stages:** Teal (#009688)
- **EXECUTION_UNDERWAY:** Indigo (#3F51B5)
- **COMPLETED:** Dark Green (#1B5E20)

### Alert Levels for Duration
- **Green:** Within expected duration (< 48 hours)
- **Yellow:** Slightly delayed (48-72 hours)
- **Orange:** Delayed (72-120 hours)
- **Red:** Critically delayed (> 120 hours)

### Interactive Features
1. **Drill-down:** Click chart segments to see detailed task list
2. **Filters:** Date range, status, partner, donor
3. **Export:** Download data as CSV/PDF
4. **Real-time updates:** Auto-refresh every 5 minutes
5. **Comparison:** Compare current period vs previous period

### Responsive Design
- **Desktop:** Full multi-chart dashboard
- **Tablet:** 2-column grid with scrollable cards
- **Mobile:** Vertical stack with swipeable cards

---

## API Endpoints Summary

| Endpoint | Method | Purpose | Chart Use |
|----------|--------|---------|-----------|
| `/api/v1/tasks/{taskId}/status-history` | GET | Get all status changes | Timeline, Table |
| `/api/v1/tasks/{taskId}/status-history/status/{status}/first-occurrence` | GET | When status first reached | Milestone markers |
| `/api/v1/tasks/{taskId}/status-history/date-range` | GET | History in date range | Time-based filters |
| `/api/v1/tasks/{taskId}/status-history/status/{status}` | GET | All times in status | Cycle analysis |
| `/api/v1/tasks/{taskId}/status-history/latest` | GET | Most recent change | Current state indicator |
| `/api/v1/tasks/{taskId}/status-history/status/{status}/duration` | GET | Total time in status | Duration breakdown |

---

## Implementation Steps

### Backend (✅ COMPLETED)
1. ✅ Created `TaskStatusHistory` entity
2. ✅ Created `TaskStatusHistoryRepository` with query methods
3. ✅ Created `TaskStatusHistoryService` interface and implementation
4. ✅ Created `TaskStatusHistoryController` REST API
6. ✅ Updated `Task` entity with `changeStatus()` method
7. ✅ Updated `TaskServiceImpl` to track status changes
8. ✅ Updated `TaskResponseDto` to include statusHistory field
9. ✅ Created database migration (002-add-task-status-history.yaml)
10. ✅ Created DTO and Mapper for responses

### Frontend (TODO)
1. Create status history service to call API endpoints
2. Implement timeline component for task detail view
3. Create dashboard charts for analytics
4. Add status change modal with notes field
5. Implement real-time status board (Kanban)
6. Add export functionality for reports

### Testing (TODO)
1. Unit tests for TaskStatusHistoryService
2. Integration tests for status transition tracking
3. API endpoint tests
4. Performance tests for history queries

---

## Example Frontend Implementation (Angular/React)

### Service Call (TypeScript)
```typescript
// Get status history for a task
getTaskStatusHistory(taskId: number): Observable<TaskStatusHistory[]> {
  return this.http.get<TaskStatusHistory[]>(
    `${this.apiUrl}/tasks/${taskId}/status-history`
  );
}

// Get duration in specific status
getDurationInStatus(taskId: number, status: string): Observable<number> {
  return this.http.get<number>(
    `${this.apiUrl}/tasks/${taskId}/status-history/status/${status}/duration`
  );
}
```

### Timeline Component (Chart.js Config)
```javascript
const timelineConfig = {
  type: 'bar',
  data: {
    datasets: statusHistory.map(h => ({
      label: h.toStatusDisplay,
      data: [{
        x: [h.changedAt, h.nextChangedAt || new Date()],
        y: h.toStatus
      }],
      backgroundColor: getStatusColor(h.toStatus),
      borderColor: getStatusColor(h.toStatus),
    }))
  },
  options: {
    indexAxis: 'y',
    scales: {
      x: { type: 'time' }
    },
    plugins: {
      tooltip: {
        callbacks: {
          label: (context) => [
            `Duration: ${context.raw.durationInPreviousStatusHours}h`,
            `Changed by: ${context.raw.changedBy}`,
            `Notes: ${context.raw.notes}`
          ]
        }
      }
    }
  }
};
```

---

## Reporting Queries

### Monthly Status Transition Report
```sql
SELECT 
    DATE_TRUNC('month', changed_at) as month,
    from_status,
    to_status,
    COUNT(*) as transitions,
    AVG(duration_in_previous_status_hours) as avg_duration
FROM task_status_history
WHERE changed_at >= CURRENT_DATE - INTERVAL '12 months'
GROUP BY month, from_status, to_status
ORDER BY month DESC, transitions DESC;
```

### SLA Compliance Report
```sql
WITH status_sla AS (
    SELECT 'TASK_UNDER_REVIEW' as status, 48 as sla_hours UNION ALL
    SELECT 'ALLOCATED', 24 UNION ALL
    SELECT 'CONCEPT_NOTE_UNDER_REVIEW', 72 UNION ALL
    SELECT 'INCEPTION_REPORT_UNDER_REVIEW', 72
)
SELECT 
    tsh.to_status,
    ss.sla_hours,
    AVG(tsh.duration_in_previous_status_hours) as avg_actual_hours,
    COUNT(*) as total_transitions,
    SUM(CASE WHEN tsh.duration_in_previous_status_hours <= ss.sla_hours THEN 1 ELSE 0 END) as within_sla,
    ROUND(
        100.0 * SUM(CASE WHEN tsh.duration_in_previous_status_hours <= ss.sla_hours THEN 1 ELSE 0 END) / COUNT(*),
        2
    ) as sla_compliance_percentage
FROM task_status_history tsh
JOIN status_sla ss ON tsh.to_status::text = ss.status
WHERE tsh.duration_in_previous_status_hours IS NOT NULL
GROUP BY tsh.to_status, ss.sla_hours;
```

**Chart Type:** Gauge Chart or Progress Bar
- Shows SLA compliance percentage per status
- Red/Yellow/Green zones
- Target line at 95% compliance

---

## Advanced Features

### Predictive Analytics
- Use historical duration data to predict completion dates
- Alert when tasks exceed 90% of typical duration
- Machine learning model to identify at-risk tasks

### Automated Alerts
- Email/Slack notification when task stuck > threshold
- Daily summary of status changes
- Weekly performance report

### Custom Workflows
- Define expected status flow per task type
- Alert on unexpected transitions
- Track compliance with defined workflow

