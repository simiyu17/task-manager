import { Component, OnInit, ViewChild, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { TasksStepperComponent } from '../tasks-stepper/tasks-stepper.component';
import { TaskService } from '../../../../services/task/task.service';

@Component({
  selector: 'app-edit-task',
  standalone: true,
  imports: [CommonModule, TasksStepperComponent],
  templateUrl: './edit-task.component.html',
  styleUrl: './edit-task.component.scss'
})
export class EditTaskComponent implements OnInit {
  @ViewChild(TasksStepperComponent) stepper!: TasksStepperComponent;
  
  taskId: string = '';
  task: any = null;
  loading: boolean = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private taskService: TaskService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.taskId = this.route.snapshot.paramMap.get('id') || '';
    if (this.taskId) {
      this.loadTask();
    }
  }

  loadTask(): void {
    this.loading = true;
    this.taskService.getTask(this.taskId).subscribe({
      next: (response) => {
        console.log('Task loaded for editing:', response);
        this.task = response;
        this.loading = false;
        this.cdr.detectChanges();
        this.populateForm();
      },
      error: (error) => {
        console.error('Error loading task:', error);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  populateForm(): void {
    // Wait for the stepper component to be initialized
    setTimeout(() => {
      if (this.stepper && this.stepper.initiateTaskComponent && this.task) {
        const form = this.stepper.initiateTaskComponent.taskForm;
        form.patchValue({
          title: this.task.title,
          description: this.task.description,
          taskProviderName: this.task.taskProviderName,
          validatedBudget: this.task.validatedBudget,
          deadline: this.task.deadline ? this.task.deadline.split('T')[0] : null
        });
      }
    }, 100);
  }

  onTaskUpdated(taskId: string): void {
    // Navigate back to view task after successful update
    this.router.navigate(['/base/tasks', taskId, 'view']);
  }

  onCancel(): void {
    this.router.navigate(['/base/tasks', this.taskId, 'view']);
  }
}
