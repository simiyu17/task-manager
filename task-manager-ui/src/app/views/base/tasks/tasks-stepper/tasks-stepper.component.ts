import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  CardBodyComponent,
  CardComponent,
  CardHeaderComponent,
  ColComponent,
  RowComponent,
  ButtonDirective
} from '@coreui/angular';

interface Step {
  id: number;
  title: string;
  completed: boolean;
}

@Component({
  selector: 'app-tasks-stepper',
  imports: [
    CommonModule,
    RowComponent,
    ColComponent,
    CardComponent,
    CardHeaderComponent,
    CardBodyComponent,
    ButtonDirective
  ],
  templateUrl: './tasks-stepper.component.html',
  styleUrl: './tasks-stepper.component.scss',
})
export class TasksStepperComponent {
  currentStep = 1;
  
  steps: Step[] = [
    { id: 1, title: 'Project Initialization', completed: false },
    { id: 2, title: 'Requirements Analysis', completed: false },
    { id: 3, title: 'Design Architecture', completed: false },
    { id: 4, title: 'Database Schema', completed: false },
    { id: 5, title: 'API Development', completed: false },
    { id: 6, title: 'Frontend Integration', completed: false },
    { id: 7, title: 'Testing Phase', completed: false },
    { id: 8, title: 'Quality Assurance', completed: false },
    { id: 9, title: 'Deployment Setup', completed: false },
    { id: 10, title: 'Production Release', completed: false }
  ];

  nextStep(): void {
    if (this.currentStep < this.steps.length) {
      this.steps[this.currentStep - 1].completed = true;
      this.currentStep++;
    }
  }

  previousStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
    }
  }

  goToStep(stepId: number): void {
    if (stepId <= this.currentStep || this.steps[stepId - 2]?.completed) {
      this.currentStep = stepId;
    }
  }

  isStepAccessible(stepId: number): boolean {
    return stepId <= this.currentStep || (stepId > 1 && this.steps[stepId - 2]?.completed);
  }

  getStepStatus(step: Step): string {
    if (step.completed) return 'completed';
    if (step.id === this.currentStep) return 'active';
    return 'pending';
  }
}
