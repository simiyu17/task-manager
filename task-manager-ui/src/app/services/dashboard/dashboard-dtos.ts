export interface DashboardKpiDto {
  totalActiveTasks: number;
  totalCompletedTasks: number;
  tasksStuck: number;
  averageCompletionTimeHours: number;
  totalActiveBudget: number;
  tasksNearingDeadline: number;
  partnersActive: number;
  donorsActive: number;
  completionRatePercentage: number;
  tasksCreatedThisMonth: number;
  tasksCompletedThisMonth: number;
}

export interface StatusDistributionDto {
  status: string;
  statusDisplay: string;
  taskCount: number;
  percentage: number;
  averageBudget: number;
}

export interface StatusDurationDto {
  status: string;
  statusDisplay: string;
  averageHours: number;
  minHours: number;
  maxHours: number;
  medianHours: number;
  transitionCount: number;
}

export interface StuckTaskDto {
  taskId: number;
  title: string;
  currentStatus: string;
  statusDisplay: string;
  statusSince: string;
  hoursInStatus: number;
  urgencyLevel: string;
  partnerName: string;
  donorName: string;
  deadline: string;
  changedBy: string;
}

export interface StatusTransitionDto {
  fromStatus: string;
  fromStatusDisplay: string;
  toStatus: string;
  toStatusDisplay: string;
  transitionCount: number;
  averageDurationHours: number;
}

export interface PartnerPerformanceDto {
  partnerId: number;
  partnerName: string;
  totalTasks: number;
  completedTasks: number;
  activeTasks: number;
  averageCompletionHours: number;
  completionRate: number;
  totalBudget: string;
}

export interface CompletionTrendDto {
  periodStart: string;
  completedTasks: number;
  averageDurationHours: number;
  averageDurationDays: number;
}

export interface StatusActivityDto {
  date: string;
  status: string;
  statusDisplay: string;
  changesCount: number;
}

export interface UserActivityDto {
  username: string;
  status: string;
  statusDisplay: string;
  changesMade: number;
  firstChange: string;
  lastChange: string;
}

export interface ActivityCalendarDto {
  date: string;
  changesCount: number;
  uniqueTasks: number;
  uniqueUsers: number;
}

export interface TaskDeadlineDto {
  taskId: number;
  title: string;
  status: string;
  statusDisplay: string;
  deadline: string;
  daysRemaining: number;
  partnerName: string;
  donorName: string;
  priority: string;
}

export interface DonorActivityDto {
  donorId: number;
  donorName: string;
  emailAddress: string;
  totalRequests: number;
  activeRequests: number;
  completedRequests: number;
  totalBudget: string;
  lastRequestDate: string;
}

export interface RecentActivityDto {
  historyId: number;
  taskId: number;
  taskTitle: string;
  fromStatus: string;
  fromStatusDisplay: string;
  toStatus: string;
  toStatusDisplay: string;
  changedAt: string;
  changedBy: string;
  durationInPreviousStatusHours: number;
}

export interface WorkloadDistributionDto {
  date: string;
  status: string;
  statusDisplay: string;
  taskCount: number;
}

export interface DashboardFilters {
  donorId?: number;
  assignedPartnerId?: number;
  fromDate?: string;
  toDate?: string;
}
