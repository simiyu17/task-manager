import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
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
  PaginationComponent
} from '@coreui/angular';

interface Task {
  id: number;
  taskName: string;
  assignedTo: string;
  priority: string;
  status: string;
  dueDate: string;
  progress: number;
}

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
    PageLinkDirective
  ],
  templateUrl: './tasks.component.html',
  styleUrl: './tasks.component.scss',
})
export class TasksComponent implements OnInit {
  tasks: Task[] = [];
  paginatedTasks: Task[] = [];
  currentPage = 1;
  itemsPerPage = 10;
  totalPages = 0;
  Math = Math;

  ngOnInit(): void {
    this.generateMockTasks();
    this.updatePagination();
  }

  generateMockTasks(): void {
    const taskNames = [
      'Implement User Authentication',
      'Design Database Schema',
      'Create API Endpoints',
      'Build Frontend Components',
      'Write Unit Tests',
      'Setup CI/CD Pipeline',
      'Perform Code Review',
      'Update Documentation',
      'Fix Bug in Payment Module',
      'Optimize Database Queries',
      'Integrate Third-party API',
      'Refactor Legacy Code',
      'Create User Dashboard',
      'Implement Search Feature',
      'Setup Monitoring Tools',
      'Configure Load Balancer',
      'Migrate to Microservices',
      'Implement Caching Strategy',
      'Security Audit',
      'Performance Testing',
      'Deploy to Production',
      'Create Admin Panel',
      'Implement Notification System',
      'Setup Backup Strategy',
      'Mobile App Integration',
      'Implement Analytics',
      'Create Reports Module',
      'Setup Email Templates',
      'Implement File Upload',
      'Create Export Feature'
    ];

    const assignees = ['John Doe', 'Jane Smith', 'Mike Johnson', 'Sarah Williams', 'David Brown', 'Emily Davis', 'Chris Wilson', 'Amanda Taylor'];
    const priorities = ['High', 'Medium', 'Low', 'Critical'];
    const statuses = ['In Progress', 'Completed', 'Pending', 'On Hold', 'Review'];

    for (let i = 0; i < 30; i++) {
      this.tasks.push({
        id: i + 1,
        taskName: taskNames[i],
        assignedTo: assignees[Math.floor(Math.random() * assignees.length)],
        priority: priorities[Math.floor(Math.random() * priorities.length)],
        status: statuses[Math.floor(Math.random() * statuses.length)],
        dueDate: this.getRandomDate(),
        progress: Math.floor(Math.random() * 101)
      });
    }
  }

  getRandomDate(): string {
    const start = new Date(2026, 1, 1);
    const end = new Date(2026, 11, 31);
    const date = new Date(start.getTime() + Math.random() * (end.getTime() - start.getTime()));
    return date.toISOString().split('T')[0];
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

  onEdit(task: Task): void {
    console.log('Edit task:', task);
    // Implement edit logic
  }

  onDelete(task: Task): void {
    console.log('Delete task:', task);
    // Implement delete logic
    this.tasks = this.tasks.filter(t => t.id !== task.id);
    this.updatePagination();
  }

  onView(task: Task): void {
    console.log('View task:', task);
    // Implement view logic
  }

  onCreate(): void {
    console.log('Create new task');
    // Implement create logic
  }
}
