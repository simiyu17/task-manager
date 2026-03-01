import { Component, OnInit, ChangeDetectorRef, ViewChild } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import {
  CardBodyComponent,
  CardComponent,
  CardHeaderComponent,
  ColComponent,
  RowComponent,
  ButtonDirective,
  TableDirective,
  PageItemComponent,
  PageLinkDirective,
  PaginationComponent,
  ModalComponent,
  ModalHeaderComponent,
  ModalTitleDirective,
  ModalBodyComponent,
  ModalFooterComponent,
  ButtonCloseDirective
} from '@coreui/angular';
import { TaskService, TaskResponse } from '../../../services/task/task.service';
import { AuthService } from '../../../services/users/auth.service';
import { InitiateTaskComponent } from './initiate-task/initiate-task.component';
import { TaskRequestDto } from './dto/task-request-dto';

@Component({
  selector: 'app-tasks',
  imports: [
    CommonModule,
    RowComponent,
    ColComponent,
    CardComponent,
    CardHeaderComponent,
    CardBodyComponent,
    ButtonDirective,
    TableDirective,
    PaginationComponent,
    PageItemComponent,
    PageLinkDirective,
    ModalComponent,
    ModalHeaderComponent,
    ModalTitleDirective,
    ModalBodyComponent,
    ModalFooterComponent,
    ButtonCloseDirective,
    InitiateTaskComponent
  ],
  providers: [DatePipe],
  templateUrl: './tasks.component.html',
  styleUrl: './tasks.component.scss',
})
export class TasksComponent implements OnInit {
  @ViewChild(InitiateTaskComponent) initiateTaskComponent?: InitiateTaskComponent;
  
  tasks: TaskResponse[] = [];
  paginatedTasks: TaskResponse[] = [];
  currentPage = 1;
  itemsPerPage = 10;
  totalPages = 0;
  Math = Math;
  isLoading = true;
  
  // Modal properties
  initiateTaskModalVisible = false;
  createdTaskId: string | null = null;

  constructor(
    private taskService: TaskService,
    private cdr: ChangeDetectorRef,
    private datePipe: DatePipe,
    private router: Router,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadTasks();
  }

  loadTasks(): void {
    this.isLoading = true;
    this.taskService.getAllTasks().subscribe({
      next: (response: TaskResponse[]) => {
        this.tasks = response.map(task => ({
          ...task,
          priority: this.calculatePriority(),
          taskStatus: this.mapTaskStatus(task.taskStatus),
          progress: this.calculateProgress()
        }));
        this.updatePagination();
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading tasks:', error);
        this.isLoading = false;
        this.cdr.detectChanges();
        // Optionally show error message to user
      }
    });
  }

  calculatePriority(): string {
    const priorities = ['High', 'Medium', 'Low', 'Critical'];
    return priorities[Math.floor(Math.random() * priorities.length)];
  }

  calculateProgress(): number {
    return Math.floor(Math.random() * 101);
  }

  mapTaskStatus(apiStatus: string): string {
    // Map API status to display status if needed
    const statusMap: { [key: string]: string } = {
      'PENDING': 'Pending',
      'IN_PROGRESS': 'In Progress',
      'COMPLETED': 'Completed',
      'ON_HOLD': 'On Hold',
      'REVIEW': 'Review'
    };
    return statusMap[apiStatus] || apiStatus;
  }

  formatDate(dateString: string): string {
    return this.datePipe.transform(dateString, 'dd MMM, yyyy') || dateString;
  }

  updatePagination(): void {
    this.totalPages = Math.ceil(this.tasks.length / this.itemsPerPage);
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    this.paginatedTasks = this.tasks.slice(startIndex, endIndex);
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.updatePagination();
    }
  }

  getPriorityColor(priority: string): string {
    switch(priority) {
      case 'Critical': return 'danger';
      case 'High': return 'warning';
      case 'Medium': return 'info';
      case 'Low': return 'secondary';
      default: return 'secondary';
    }
  }

  getStatusColor(status: string): string {
    switch(status) {
      case 'Completed': return 'success';
      case 'In Progress': return 'primary';
      case 'Pending': return 'warning';
      case 'On Hold': return 'secondary';
      case 'Review': return 'info';
      default: return 'secondary';
    }
  }

  onEdit(task: TaskResponse): void {
    this.router.navigate(['/base/tasks', task.id, 'edit']);
  }

  onDelete(task: TaskResponse): void {
    console.log('Delete task:', task);
    // Implement delete logic
    this.tasks = this.tasks.filter(t => t.id !== task.id);
    this.updatePagination();
  }

  onView(task: TaskResponse): void {
    this.router.navigate(['/base/tasks', task.id, 'view']);
  }

  // Modal methods
  openInitiateTaskModal(): void {
    this.initiateTaskModalVisible = true;
  }

  closeInitiateTaskModal(): void {
    this.initiateTaskModalVisible = false;
    this.createdTaskId = null;
  }

  handleInitiateTaskModalChange(event: boolean): void {
    this.initiateTaskModalVisible = event;
    if (!event) {
      this.createdTaskId = null;
    }
  }

  onTaskCreated(event: { success: boolean; taskId?: string; data?: TaskRequestDto }): void {
    if (event.success && event.taskId) {
      this.createdTaskId = event.taskId;
      // Close modal
      this.closeInitiateTaskModal();
      // Reload tasks to show the new one
      this.loadTasks();
      // Navigate to view task
      this.router.navigate(['/base/tasks', event.taskId, 'view']);
    }
  }

  submitInitiateTask(): void {
    if (this.initiateTaskComponent) {
      this.initiateTaskComponent.onSubmit();
    }
  }

  cancelInitiateTask(): void {
    this.closeInitiateTaskModal();
  }
}
