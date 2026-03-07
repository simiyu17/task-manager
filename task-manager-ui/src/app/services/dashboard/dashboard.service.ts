import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../api.service';
import { HttpParams } from '@angular/common/http';
import {
  DashboardKpiDto,
  StatusDistributionDto,
  StatusDurationDto,
  StuckTaskDto,
  StatusTransitionDto,
  PartnerPerformanceDto,
  CompletionTrendDto,
  StatusActivityDto,
  UserActivityDto,
  ActivityCalendarDto,
  TaskDeadlineDto,
  DonorActivityDto,
  RecentActivityDto,
  WorkloadDistributionDto,
  DashboardFilters
} from './dashboard-dtos';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private readonly API_PATH = 'dashboard';

  constructor(private apiService: ApiService) {}

  /**
   * Build HTTP params from filters
   */
  private buildParams(filters?: DashboardFilters, additionalParams?: { [key: string]: any }): HttpParams {
    let params = new HttpParams();
    
    if (filters) {
      if (filters.donorId) {
        params = params.set('donorId', filters.donorId.toString());
      }
      if (filters.assignedPartnerId) {
        params = params.set('assignedPartnerId', filters.assignedPartnerId.toString());
      }
      if (filters.fromDate) {
        params = params.set('fromDate', filters.fromDate);
      }
      if (filters.toDate) {
        params = params.set('toDate', filters.toDate);
      }
    }

    if (additionalParams) {
      Object.keys(additionalParams).forEach(key => {
        if (additionalParams[key] !== null && additionalParams[key] !== undefined) {
          params = params.set(key, additionalParams[key].toString());
        }
      });
    }

    return params;
  }

  /**
   * Get overall KPI metrics for dashboard header
   */
  getKpiMetrics(filters?: DashboardFilters): Observable<DashboardKpiDto> {
    const params = this.buildParams(filters);
    return this.apiService.get<DashboardKpiDto>(`${this.API_PATH}/kpi`, { params });
  }

  /**
   * Get current status distribution
   * Use for: Donut chart, Pie chart, Bar chart
   */
  getStatusDistribution(filters?: DashboardFilters): Observable<StatusDistributionDto[]> {
    const params = this.buildParams(filters);
    return this.apiService.get<StatusDistributionDto[]>(`${this.API_PATH}/status-distribution`, { params });
  }

  /**
   * Get average time spent in each status
   * Use for: Horizontal bar chart, Column chart
   */
  getAverageTimePerStatus(filters?: DashboardFilters): Observable<StatusDurationDto[]> {
    const params = this.buildParams(filters);
    return this.apiService.get<StatusDurationDto[]>(`${this.API_PATH}/status-duration`, { params });
  }

  /**
   * Get tasks stuck in status
   * Use for: Alert cards, Table with urgency badges
   */
  getStuckTasks(minHours: number = 72, filters?: DashboardFilters): Observable<StuckTaskDto[]> {
    const params = this.buildParams(filters, { minHours });
    return this.apiService.get<StuckTaskDto[]>(`${this.API_PATH}/stuck-tasks`, { params });
  }

  /**
   * Get status transition flow
   * Use for: Sankey diagram, Heatmap
   */
  getStatusTransitions(filters?: DashboardFilters): Observable<StatusTransitionDto[]> {
    const params = this.buildParams(filters);
    return this.apiService.get<StatusTransitionDto[]>(`${this.API_PATH}/status-transitions`, { params });
  }

  /**
   * Get partner performance metrics
   * Use for: Table, Grouped bar chart, Heatmap
   */
  getPartnerPerformance(filters?: DashboardFilters): Observable<PartnerPerformanceDto[]> {
    const params = this.buildParams(filters);
    return this.apiService.get<PartnerPerformanceDto[]>(`${this.API_PATH}/partner-performance`, { params });
  }

  /**
   * Get completion trend over time
   * Use for: Line chart, Combo chart (bar + line)
   */
  getCompletionTrend(period: string = 'week', filters?: DashboardFilters): Observable<CompletionTrendDto[]> {
    const params = this.buildParams(filters, { period });
    return this.apiService.get<CompletionTrendDto[]>(`${this.API_PATH}/completion-trend`, { params });
  }

  /**
   * Get status change activity over time
   * Use for: Stacked area chart, Multi-line chart
   */
  getStatusActivity(days: number = 30, filters?: DashboardFilters): Observable<StatusActivityDto[]> {
    const params = this.buildParams(filters, { days });
    return this.apiService.get<StatusActivityDto[]>(`${this.API_PATH}/status-activity`, { params });
  }

  /**
   * Get user activity metrics
   * Use for: Grouped bar chart, Treemap, Table
   */
  getUserActivity(days: number = 30, filters?: DashboardFilters): Observable<UserActivityDto[]> {
    const params = this.buildParams(filters, { days });
    return this.apiService.get<UserActivityDto[]>(`${this.API_PATH}/user-activity`, { params });
  }

  /**
   * Get activity calendar data
   * Use for: Calendar heatmap (GitHub-style)
   */
  getActivityCalendar(days: number = 90, filters?: DashboardFilters): Observable<ActivityCalendarDto[]> {
    const params = this.buildParams(filters, { days });
    return this.apiService.get<ActivityCalendarDto[]>(`${this.API_PATH}/activity-calendar`, { params });
  }

  /**
   * Get tasks approaching deadline
   * Use for: Alert cards, Table, Timeline
   */
  getTasksApproachingDeadline(days: number = 7, filters?: DashboardFilters): Observable<TaskDeadlineDto[]> {
    const params = this.buildParams(filters, { days });
    return this.apiService.get<TaskDeadlineDto[]>(`${this.API_PATH}/approaching-deadlines`, { params });
  }

  /**
   * Get donor activity summary
   * Use for: Table, Card list
   */
  getDonorActivity(filters?: DashboardFilters): Observable<DonorActivityDto[]> {
    const params = this.buildParams(filters);
    return this.apiService.get<DonorActivityDto[]>(`${this.API_PATH}/donor-activity`, { params });
  }

  /**
   * Get recent activity feed
   * Use for: Activity timeline, Feed list
   */
  getRecentActivity(limit: number = 20, filters?: DashboardFilters): Observable<RecentActivityDto[]> {
    const params = this.buildParams(filters, { limit });
    return this.apiService.get<RecentActivityDto[]>(`${this.API_PATH}/recent-activity`, { params });
  }

  /**
   * Get workload distribution over time
   * Use for: Stacked area chart, Stacked bar chart
   */
  getWorkloadDistribution(days: number = 30, filters?: DashboardFilters): Observable<WorkloadDistributionDto[]> {
    const params = this.buildParams(filters, { days });
    return this.apiService.get<WorkloadDistributionDto[]>(`${this.API_PATH}/workload-distribution`, { params });
  }
}
