export interface TaskRequestDto {
  title: string;
  donorId: number;
  description?: string;
  assignedPartnerId?: number;
  validatedBudget?: number;
  requestReceivedAt?: string; // ISO 8601 format
  acceptedAt?: string; // ISO 8601 format
  deadline?: string; // ISO 8601 format
  allocateNotes?: string;
  acceptanceNotes?: string;
  rejectionNotes?: string;
}