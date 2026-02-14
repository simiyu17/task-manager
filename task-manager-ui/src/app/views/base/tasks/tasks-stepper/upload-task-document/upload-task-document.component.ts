import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  ColComponent,
  RowComponent,
  FormDirective,
  FormFeedbackComponent,
  FormSelectDirective
} from '@coreui/angular';
import { DocumentService } from '../../../../../services/document/document.service';

interface DocumentType {
  value: string;
  label: string;
}

@Component({
  selector: 'app-upload-task-document',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RowComponent,
    ColComponent,
    FormDirective,
    FormFeedbackComponent,
    FormSelectDirective
  ],
  templateUrl: './upload-task-document.component.html',
  styleUrl: './upload-task-document.component.scss'
})
export class UploadTaskDocumentComponent implements OnInit {
  @Input() taskId: string = '';
  @Input() createMode: boolean = true;
  @Output() documentUploaded = new EventEmitter<{ success: boolean; message?: string }>();

  uploadForm: FormGroup;
  isSubmitting = false;
  errorMessage = '';
  selectedFileName = '';
  selectedFile: File | null = null;
  isDragging = false;

  documentTypes: DocumentType[] = [
    { value: '', label: 'Select document type' },
    { value: 'TASK_DOCUMENT', label: 'Task Document' },
    { value: 'WBS', label: 'WBS' },
    { value: 'CONCEPT_NOTE', label: 'Concept Note' },
    { value: 'INCEPTION_REPORT', label: 'Inception Report' },
    { value: 'DRAFT_REPORT', label: 'Draft Report' },
    { value: 'FINAL_REPORT', label: 'Final Report' }
  ];

  constructor(
    private fb: FormBuilder,
    private documentService: DocumentService,
    private route: ActivatedRoute
  ) {
    this.uploadForm = this.fb.group({
      documentType: ['', [Validators.required]],
      file: [null, [Validators.required]]
    });
  }

  ngOnInit(): void {
    // Get taskId from route when not in createMode
    if (!this.createMode && !this.taskId) {
      this.route.params.subscribe(params => {
        this.taskId = params['id'] || params['taskId'] || '';
      });
    }
  }

  get f() {
    return this.uploadForm.controls;
  }

  onFileSelect(event: Event) {
    const input = event.target as HTMLInputElement;
    
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      this.processFile(file);
    }
  }

  removeFile() {
    this.selectedFile = null;
    this.selectedFileName = '';
    this.uploadForm.patchValue({ file: null });
    this.errorMessage = '';
    
    // Clear file input
    const fileInput = document.getElementById('fileUpload') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      const file = files[0];
      this.processFile(file);
    }
  }

  private processFile(file: File) {
    // Validate file size (max 10MB)
    const maxSize = 10 * 1024 * 1024; // 10MB in bytes
    if (file.size > maxSize) {
      this.errorMessage = 'File size must not exceed 10MB';
      this.selectedFile = null;
      this.selectedFileName = '';
      this.uploadForm.patchValue({ file: null });
      return;
    }

    // Validate file type
    const allowedTypes = [
      'application/pdf',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      'application/vnd.ms-excel',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      'image/jpeg',
      'image/png',
      'image/jpg'
    ];

    if (!allowedTypes.includes(file.type)) {
      this.errorMessage = 'Invalid file type. Allowed types: PDF, DOC, DOCX, XLS, XLSX, JPG, JPEG, PNG';
      this.selectedFile = null;
      this.selectedFileName = '';
      this.uploadForm.patchValue({ file: null });
      return;
    }

    this.selectedFile = file;
    this.selectedFileName = file.name;
    this.uploadForm.patchValue({ file: file });
    this.errorMessage = '';
  }

  onSubmit() {
    if (this.uploadForm.invalid) {
      this.uploadForm.markAllAsTouched();
      return;
    }

    if (!this.taskId) {
      this.errorMessage = 'Task ID is missing. Please complete step 1 first.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    if (!this.selectedFile) {
      this.errorMessage = 'Please select a file to upload.';
      this.isSubmitting = false;
      return;
    }

    const documentType = this.uploadForm.value.documentType;
    const taskIdNumber = parseInt(this.taskId, 10);

    this.documentService.uploadDocument(this.selectedFile, taskIdNumber, documentType).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.documentUploaded.emit({ success: true, message: 'Document uploaded successfully' });
        // Reset form
        this.uploadForm.reset();
        this.removeFile();
      },
      error: (error) => {
        this.isSubmitting = false;
        this.errorMessage = error.error?.message || 'Failed to upload document. Please try again.';
        this.documentUploaded.emit({ success: false });
      }
    });
  }

  getFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  }
}
