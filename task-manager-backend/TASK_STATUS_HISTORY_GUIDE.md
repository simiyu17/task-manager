# Task Status History Tracking

## Overview
This feature adds comprehensive status transition tracking for tasks, allowing you to:
- Track when a task moved from one status to another
- Identify who made the status change
- Calculate time spent in each status
- Query historical status data for reporting and analytics

## Database Schema

### New Table: `task_status_history`
```sql
CREATE TABLE task_status_history (
    id BIGINT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    from_status VARCHAR(100),
    to_status VARCHAR(100) NOT NULL,
    changed_at TIMESTAMP NOT NULL,
    changed_by VARCHAR(255),
    notes TEXT,
    duration_in_previous_status_hours BIGINT,
    date_created TIMESTAMP NOT NULL,
    last_modified TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    data_status VARCHAR(50),
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);
```

### Indexes for Performance
- `idx_task_status_history_task_id` - For querying by task
- `idx_task_status_history_to_status` - For querying by status
- `idx_task_status_history_changed_at` - For time-based queries
- `idx_task_status_history_task_status` - Composite index for common queries

## API Endpoints

### 1. Get All Status History for a Task
```
GET /api/v1/tasks/{taskId}/status-history
```
Returns all status changes for a task in reverse chronological order.

**Response Example:**
```json
[
  {
    "id": 1,
    "taskId": 100,
    "taskTitle": "Implement User Authentication",
    "fromStatus": "INITIATED",
    "fromStatusDisplay": "INITIATED",
    "toStatus": "ALLOCATED",
    "toStatusDisplay": "ALLOCATED",
    "changedAt": "2026-03-07T10:30:00",
    "changedBy": "admin@example.com",
    "notes": "Status updated via API",
    "durationInPreviousStatusHours": 24
  }
]
```

### 2. Get First Occurrence of a Status
```
GET /api/v1/tasks/{taskId}/status-history/status/{status}/first-occurrence
```
Returns the timestamp when the task first reached the specified status.

**Example:**
```
GET /api/v1/tasks/100/status-history/status/ACCEPTED/first-occurrence
```

**Response:** `2026-03-05T14:30:00`

### 3. Get Status History Within Date Range
```
GET /api/v1/tasks/{taskId}/status-history/date-range?startDate={start}&endDate={end}
```

**Example:**
```
GET /api/v1/tasks/100/status-history/date-range?startDate=2026-03-01T00:00:00&endDate=2026-03-07T23:59:59
```

### 4. Get All Times Task Was in a Specific Status
```
GET /api/v1/tasks/{taskId}/status-history/status/{status}
```
Returns all occurrences when the task was in the specified status (useful for tasks that cycle back).

### 5. Get Latest Status Change
```
GET /api/v1/tasks/{taskId}/status-history/latest
```
Returns the most recent status change for a task.

### 6. Get Total Duration in a Status
```
GET /api/v1/tasks/{taskId}/status-history/status/{status}/duration
```
Returns the total time (in hours) a task spent in a specific status.

**Example Response:** `72` (hours)

## Usage in Code

### Updating Task Status (Automatic History Tracking)
```java
@Autowired
private TaskService taskService;

// This automatically creates a history record
taskService.updateTaskStatus(taskId, "ALLOCATED");
```

### Querying Status History
```java
@Autowired
private TaskStatusHistoryService statusHistoryService;

// Get when task was first accepted
Optional<LocalDateTime> acceptedDate = statusHistoryService
    .getDateWhenTaskReachedStatus(taskId, TaskStatus.ACCEPTED);

// Get total time spent in review
Long hoursInReview = statusHistoryService
    .getTotalDurationInStatus(taskId, TaskStatus.TASK_UNDER_REVIEW);

// Get full history
List<TaskStatusHistoryResponseDto> history = statusHistoryService
    .getTaskStatusHistory(taskId);
```

## Dashboard Queries

### Average Time in Each Status
```sql
SELECT 
    to_status,
    AVG(duration_in_previous_status_hours) as avg_hours,
    COUNT(*) as transition_count
FROM task_status_history
WHERE from_status IS NOT NULL
GROUP BY to_status
ORDER BY to_status;
```

### Task Lifecycle Timeline
```sql
SELECT 
    t.title,
    tsh.from_status,
    tsh.to_status,
    tsh.changed_at,
    tsh.changed_by,
    tsh.duration_in_previous_status_hours
FROM tasks t
JOIN task_status_history tsh ON t.id = tsh.task_id
WHERE t.id = ?
ORDER BY tsh.changed_at;
```

### Slowest Status Transitions
```sql
SELECT 
    t.title,
    tsh.from_status,
    tsh.to_status,
    tsh.duration_in_previous_status_hours,
    tsh.changed_at
FROM task_status_history tsh
JOIN tasks t ON tsh.task_id = t.id
WHERE tsh.duration_in_previous_status_hours IS NOT NULL
ORDER BY tsh.duration_in_previous_status_hours DESC
LIMIT 10;
```

### Status Change Activity by User
```sql
SELECT 
    changed_by,
    to_status,
    COUNT(*) as changes_made,
    MIN(changed_at) as first_change,
    MAX(changed_at) as last_change
FROM task_status_history
GROUP BY changed_by, to_status
ORDER BY changed_by, to_status;
```

## Chart Recommendations for Status History

### 1. Task Lifecycle Timeline
**Chart Type:** Gantt Chart or Timeline Chart
- Shows each status as a horizontal bar with duration
- Color-coded by status type
- Interactive to show details on hover

### 2. Time Spent in Each Status
**Chart Type:** Horizontal Bar Chart
- X-axis: Hours spent
- Y-axis: Status name
- Color intensity based on duration

### 3. Status Transition Flow
**Chart Type:** Sankey Diagram
- Shows flow from one status to another
- Width represents number of transitions
- Helps identify common paths and bottlenecks

### 4. Status Change Activity Over Time
**Chart Type:** Line Chart or Area Chart
- X-axis: Date/Time
- Y-axis: Number of status changes
- Multiple lines for different statuses

### 5. Average Duration by Status
**Chart Type:** Column Chart with Target Line
- Shows average time spent in each status
- Add a target/SLA line for comparison
- Highlight statuses exceeding targets in red

### 6. Status Distribution at Point in Time
**Chart Type:** Stacked Area Chart
- X-axis: Date
- Y-axis: Number of tasks
- Each area represents tasks in a specific status
- Shows workload distribution over time

## Benefits

1. **Accountability** - Track who changed status and when
2. **Performance Metrics** - Calculate average time in each status
3. **Bottleneck Identification** - Find where tasks get stuck
4. **Audit Trail** - Complete history of all status changes
5. **Reporting** - Generate lifecycle reports for stakeholders
6. **SLA Monitoring** - Track if tasks meet time expectations
7. **Process Improvement** - Analyze patterns to optimize workflow

## Migration

The database migration is automatically applied via Liquibase:
- File: `002-add-task-status-history.yaml`
- Creates table, sequences, indexes, and foreign keys
- Includes rollback script for safe deployment

To apply the migration, simply start the application - Liquibase will handle it automatically.

