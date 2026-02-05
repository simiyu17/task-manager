export interface TaskRequestDto {
  title: string;
  description?: string;
  taskProviderName: string;
  validatedBudget?: number;
  deadline?: string; // ISO 8601 format
}