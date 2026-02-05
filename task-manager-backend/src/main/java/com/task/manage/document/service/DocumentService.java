package com.task.manage.document.service;

import com.task.manage.document.domain.Document;
import com.task.manage.document.domain.DocumentRepository;
import com.task.manage.document.dto.DocumentResponseDto;
import com.task.manage.document.exception.DocumentNotFoundException;
import com.task.manage.document.mapper.DocumentMapper;
import com.task.manage.task.domain.Task;
import com.task.manage.task.domain.TaskRepository;
import com.task.manage.task.exception.TaskNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final TaskRepository taskRepository;
    private final FileStorageService fileStorageService;
    private final DocumentMapper documentMapper;

    public DocumentResponseDto uploadDocument(MultipartFile file, Long taskId, String documentTypeStr) {
        log.info("Uploading document for task: {} with type: {}", taskId, documentTypeStr);

        // Convert string to enum
        Document.DocumentType documentType = Document.DocumentType.fromString(documentTypeStr);
        if (documentType == null) {
            throw new IllegalArgumentException("Invalid document type: " + documentTypeStr);
        }

        // Validate task exists
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        // Get the next version number
        Integer version = documentRepository.findMaxVersionByTaskIdAndDocumentType(taskId, documentType)
                .map(v -> v + 1)
                .orElse(1);

        // Store the file
        String filePath = fileStorageService.storeFile(file, taskId, documentType.name(), version);

        // Get current user
        String uploadedBy = getCurrentUsername();

        // Create document entity
        Document document = Document.builder()
                .task(task)
                .version(version)
                .documentType(documentType)
                .filePath(filePath)
                .fileLocation(Document.FileLocation.LOCAL_DISK)
                .isFinal(false)
                .uploadedBy(uploadedBy)
                .uploadedAt(LocalDateTime.now())
                .build();

        Document savedDocument = documentRepository.save(document);
        log.info("Document uploaded successfully with id: {}", savedDocument.getId());

        return documentMapper.toResponseDto(savedDocument);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponseDto> getDocumentsByTaskId(Long taskId) {
        log.info("Fetching documents for task: {}", taskId);

        List<Document> documents = documentRepository.findByTaskId(taskId);
        return documents.stream()
                .map(documentMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentResponseDto getDocumentById(Long documentId) {
        log.info("Fetching document with id: {}", documentId);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        return documentMapper.toResponseDto(document);
    }

    @Transactional(readOnly = true)
    public Resource downloadDocument(Long documentId) {
        log.info("Downloading document with id: {}", documentId);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        return fileStorageService.loadFileAsResource(document.getFilePath());
    }

    @Transactional(readOnly = true)
    public boolean isDocumentPdf(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        return fileStorageService.isPdf(document.getFilePath());
    }

    public void markDocumentAsFinal(Long documentId) {
        log.info("Marking document as final with id: {}", documentId);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        document.setFinal(true);
        documentRepository.save(document);
        log.info("Document marked as final");
    }


    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}
