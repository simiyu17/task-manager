import { Component, DestroyRef, DOCUMENT, effect, inject, OnInit, Renderer2, signal, WritableSignal, ChangeDetectorRef } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ChartOptions, ChartData } from 'chart.js';
import {
  ButtonDirective,
  ButtonGroupComponent,
  CardBodyComponent,
  CardComponent,
  CardHeaderComponent,
  ColComponent,
  FormCheckLabelDirective,
  FormLabelDirective,
  FormSelectDirective,
  GutterDirective,
  ProgressComponent,
  RowComponent,
  TableDirective,
  FormDirective,
  WidgetStatFComponent,
  TemplateIdDirective
} from '@coreui/angular';
import { ChartjsComponent } from '@coreui/angular-chartjs';
import { IconDirective } from '@coreui/icons-angular';
import { CommonModule } from '@angular/common';

import { DashboardChartsData, IChartProps } from './dashboard-charts-data';
import { DashboardService } from '../../services/dashboard/dashboard.service';
import { DonorService, DonorResponseDto } from '../../services/donor/donor.service';
import { PartnerService, PartnerResponseDto } from '../../services/partner/partner.service';
import { 
  DashboardKpiDto, 
  StatusDistributionDto, 
  CompletionTrendDto,
  StuckTaskDto,
  PartnerPerformanceDto,
  DashboardFilters 
} from '../../services/dashboard/dashboard-dtos';
import { forkJoin, debounceTime, distinctUntilChanged } from 'rxjs';

interface IUser {
  name: string;
  state: string;
  registered: string;
  country: string;
  usage: number;
  period: string;
  payment: string;
  activity: string;
  avatar: string;
  status: string;
  color: string;
}

@Component({
  templateUrl: 'dashboard.component.html',
  styleUrls: ['dashboard.component.scss'],
  imports: [
    CommonModule,
    CardComponent, 
    CardBodyComponent, 
    RowComponent, 
    ColComponent, 
    ButtonDirective, 
    IconDirective, 
    ReactiveFormsModule, 
    ButtonGroupComponent, 
    FormCheckLabelDirective, 
    FormLabelDirective,
    FormSelectDirective,
    ChartjsComponent,
    GutterDirective, 
    ProgressComponent,
    CardHeaderComponent, 
    TableDirective,
    FormDirective,
    WidgetStatFComponent,
    TemplateIdDirective
  ]
})
export class DashboardComponent implements OnInit {

  readonly #destroyRef: DestroyRef = inject(DestroyRef);
  readonly #document: Document = inject(DOCUMENT);
  readonly #renderer: Renderer2 = inject(Renderer2);
  readonly #chartsData: DashboardChartsData = inject(DashboardChartsData);
  readonly #dashboardService: DashboardService = inject(DashboardService);
  readonly #donorService: DonorService = inject(DonorService);
  readonly #partnerService: PartnerService = inject(PartnerService);
  readonly #cdr: ChangeDetectorRef = inject(ChangeDetectorRef);

  // Dashboard data
  public kpiData?: DashboardKpiDto;
  public statusDistribution: StatusDistributionDto[] = [];
  public completionTrend: CompletionTrendDto[] = [];
  public stuckTasks: StuckTaskDto[] = [];
  public partnerPerformance: PartnerPerformanceDto[] = [];
  
  // Filter data
  public donors: DonorResponseDto[] = [];
  public partners: PartnerResponseDto[] = [];
  
  // Filter form
  public filterForm = new FormGroup({
    donorId: new FormControl<number | null>(null),
    assignedPartnerId: new FormControl<number | null>(null),
    fromDate: new FormControl<string | null>(null),
    toDate: new FormControl<string | null>(null)
  });

  // Loading states
  public isLoading = false;
  public isFilterLoading = false;

  public mainChart: IChartProps = { type: 'line' };
  public statusChartData: ChartData | null = null;
  public chartOptions = {
    maintainAspectRatio: false,
    datasets: {
      doughnut: {
        clip: 0
      }
    }
  };
  public mainChartRef: WritableSignal<any> = signal(undefined);
  #mainChartRefEffect = effect(() => {
    if (this.mainChartRef()) {
      this.setChartStyles();
    }
  });
  public chart: Array<IChartProps> = [];
  public trafficRadioGroup = new FormGroup({
    trafficRadio: new FormControl('Month')
  });

  ngOnInit(): void {
    this.loadFilterData();
    this.loadDashboardData();
    this.initCharts();
    this.updateChartOnColorModeChange();
    this.setupFilterChangeListener();
  }

  /**
   * Setup listener for filter changes to auto-reload data
   */
  setupFilterChangeListener(): void {
    this.filterForm.valueChanges
      .pipe(
        debounceTime(500),
        distinctUntilChanged((prev, curr) => JSON.stringify(prev) === JSON.stringify(curr))
      )
      .subscribe(() => {
        this.loadDashboardData();
      });
  }

  /**
   * Load donors and partners for filter dropdowns
   */
  loadFilterData(): void {
    this.isFilterLoading = true;
    forkJoin({
      donors: this.#donorService.getAllDonors(),
      partners: this.#partnerService.getAllPartners()
    }).subscribe({
      next: (result) => {
        this.donors = result.donors;
        this.partners = result.partners;
        this.isFilterLoading = false;
      },
      error: (error) => {
        console.error('Error loading filter data:', error);
        this.isFilterLoading = false;
      }
    });
  }

  /**
   * Load all dashboard data based on current filters
   */
  loadDashboardData(): void {
    this.isLoading = true;
    const filters: DashboardFilters = {
      donorId: this.filterForm.value.donorId ?? undefined,
      assignedPartnerId: this.filterForm.value.assignedPartnerId ?? undefined,
      fromDate: this.filterForm.value.fromDate ?? undefined,
      toDate: this.filterForm.value.toDate ?? undefined
    };

    forkJoin({
      kpi: this.#dashboardService.getKpiMetrics(filters),
      statusDistribution: this.#dashboardService.getStatusDistribution(filters),
      completionTrend: this.#dashboardService.getCompletionTrend('week', filters),
      stuckTasks: this.#dashboardService.getStuckTasks(72, filters),
      partnerPerformance: this.#dashboardService.getPartnerPerformance(filters)
    }).subscribe({
      next: (result) => {
        this.kpiData = result.kpi;
        this.statusDistribution = result.statusDistribution;
        this.completionTrend = result.completionTrend;
        this.stuckTasks = result.stuckTasks;
        this.partnerPerformance = result.partnerPerformance;
        
        // Update charts with real data
        this.buildStatusChart();
        this.buildCompletionTrendChart();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading dashboard data:', error);
        this.isLoading = false;
      }
    });
  }

  /**
   * Clear all filters and reload data
   */
  clearFilters(): void {
    this.filterForm.reset();
    this.loadDashboardData();
  }

  /**
   * Build status distribution donut chart
   */
  buildStatusChart(): void {
    if (!this.statusDistribution || this.statusDistribution.length === 0) {
      this.statusChartData = null;
      return;
    }

    const labels = this.statusDistribution.map(s => s.statusDisplay);
    const data = this.statusDistribution.map(s => s.taskCount);
    const colors = this.getStatusColors().slice(0, data.length);

    this.statusChartData = {
      labels: labels,
      datasets: [{
        data: data,
        backgroundColor: colors,
        clip: 0
      }]
    };
  }

  /**
   * Build completion trend line chart
   */
  buildCompletionTrendChart(): void {
    if (this.completionTrend.length === 0) {
      this.initCharts(); // Fall back to demo data
      return;
    }

    const labels = this.completionTrend.map(t => new Date(t.periodStart).toLocaleDateString());
    const completedData = this.completionTrend.map(t => t.completedTasks);
    const durationData = this.completionTrend.map(t => t.averageDurationDays);

    this.mainChart = {
      type: 'line',
      data: {
        labels: labels,
        datasets: [
          {
            label: 'Completed Tasks',
            data: completedData,
            backgroundColor: 'rgba(54, 162, 235, 0.2)',
            borderColor: 'rgba(54, 162, 235, 1)',
            borderWidth: 2,
            fill: true,
            clip: 0,
            yAxisID: 'y'
          },
          {
            label: 'Avg Duration (Days)',
            data: durationData,
            backgroundColor: 'rgba(255, 159, 64, 0.2)',
            borderColor: 'rgba(255, 159, 64, 1)',
            borderWidth: 2,
            fill: false,
            clip: 0,
            yAxisID: 'y1'
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {
          mode: 'index',
          intersect: false,
        },
        plugins: {
          legend: {
            display: true,
            position: 'top'
          },
          tooltip: {
            mode: 'index',
            intersect: false
          }
        },
        scales: {
          y: {
            type: 'linear',
            display: true,
            position: 'left',
            title: {
              display: true,
              text: 'Tasks Completed'
            }
          },
          y1: {
            type: 'linear',
            display: true,
            position: 'right',
            title: {
              display: true,
              text: 'Days'
            },
            grid: {
              drawOnChartArea: false,
            },
          }
        }
      }
    };
  }

  /**
   * Get color palette for status chart
   */
  getStatusColors(): string[] {
    return [
      'rgba(54, 162, 235, 0.8)',   // Blue
      'rgba(255, 206, 86, 0.8)',   // Yellow
      'rgba(75, 192, 192, 0.8)',   // Teal
      'rgba(153, 102, 255, 0.8)',  // Purple
      'rgba(255, 159, 64, 0.8)',   // Orange
      'rgba(255, 99, 132, 0.8)',   // Red
      'rgba(201, 203, 207, 0.8)',  // Grey
      'rgba(100, 181, 246, 0.8)',  // Light Blue
    ];
  }

  /**
   * Get urgency color based on urgency level
   */
  getUrgencyColor(urgencyLevel: string): string {
    switch (urgencyLevel?.toUpperCase()) {
      case 'CRITICAL': return 'danger';
      case 'HIGH': return 'warning';
      case 'MEDIUM': return 'info';
      default: return 'secondary';
    }
  }

  /**
   * Check if status chart has data to display
   */
  hasStatusChartData(): boolean {
    return !!(this.statusChartData && this.statusChartData.datasets?.[0]?.data && this.statusChartData.datasets[0].data.length > 0);
  }

  /**
   * Format hours to days and hours
   */
  formatHours(hours: number): string {
    if (hours < 24) {
      return `${hours.toFixed(1)}h`;
    }
    const days = Math.floor(hours / 24);
    const remainingHours = Math.floor(hours % 24);
    return `${days}d ${remainingHours}h`;
  }

  initCharts(): void {
    this.mainChartRef()?.stop();
    // Only use demo data if no real data is loaded
    if (this.completionTrend.length === 0) {
      this.mainChart = this.#chartsData.mainChart;
    }
  }

  setTrafficPeriod(value: string): void {
    this.trafficRadioGroup.setValue({ trafficRadio: value });
    
    // Map UI period to API period
    const periodMap: { [key: string]: string } = {
      'Day': 'day',
      'Month': 'month',
      'Year': 'month' // Using month for year view as well
    };
    
    const filters: DashboardFilters = {
      donorId: this.filterForm.value.donorId ?? undefined,
      assignedPartnerId: this.filterForm.value.assignedPartnerId ?? undefined,
      fromDate: this.filterForm.value.fromDate ?? undefined,
      toDate: this.filterForm.value.toDate ?? undefined
    };
    
    this.#dashboardService.getCompletionTrend(periodMap[value], filters).subscribe({
      next: (data) => {
        this.completionTrend = data;
        this.buildCompletionTrendChart();
      },
      error: (error) => {
        console.error('Error loading completion trend:', error);
        // Fall back to demo chart
        this.#chartsData.initMainChart(value);
        this.initCharts();
      }
    });
  }

  handleChartRef($chartRef: any) {
    if ($chartRef) {
      this.mainChartRef.set($chartRef);
    }
  }

  updateChartOnColorModeChange() {
    const unListen = this.#renderer.listen(this.#document.documentElement, 'ColorSchemeChange', () => {
      this.setChartStyles();
    });

    this.#destroyRef.onDestroy(() => {
      unListen();
    });
  }

  setChartStyles() {
    if (this.mainChartRef()) {
      setTimeout(() => {
        const options: ChartOptions = { ...this.mainChart.options };
        const scales = this.#chartsData.getScales();
        this.mainChartRef().options.scales = { ...options.scales, ...scales };
        this.mainChartRef().update();
      });
    }
  }
}
