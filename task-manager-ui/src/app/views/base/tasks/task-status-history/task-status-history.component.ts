import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TaskStatusHistoryEntry } from '../../../../services/task/task.service';

@Component({
  selector: 'app-task-status-history',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './task-status-history.component.html',
  styleUrl: './task-status-history.component.scss'
})
export class TaskStatusHistoryComponent {
  @Input() history: TaskStatusHistoryEntry[] = [];
}
