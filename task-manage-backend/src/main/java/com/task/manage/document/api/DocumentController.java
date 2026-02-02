package com.task.manage.document.api;

import com.task.manage.document.dto.DocumentResponseDto;
import com.task.manage.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponseDto> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("taskId") Long taskId,
            @RequestParam("documentType") String documentType) {

        DocumentResponseDto response = documentService.uploadDocument(file, taskId, documentType);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<DocumentResponseDto>> getDocumentsByTaskId(
            @PathVariable Long taskId) {
        List<DocumentResponseDto> documents = documentService.getDocumentsByTaskId(taskId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponseDto> getDocumentById(
            @PathVariable Long documentId) {
        DocumentResponseDto document = documentService.getDocumentById(documentId);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long documentId) {
        Resource resource = documentService.downloadDocument(documentId);

        // Determine content type
        String contentType = "application/octet-stream";
        if (documentService.isDocumentPdf(documentId)) {
            contentType = "application/pdf";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/{documentId}/view")
    public ResponseEntity<Resource> viewDocument(
            @PathVariable Long documentId) {
        Resource resource = documentService.downloadDocument(documentId);

        // Only PDFs should be viewed inline
        if (!documentService.isDocumentPdf(documentId)) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PatchMapping("/{documentId}/mark-final")
    public ResponseEntity<Void> markDocumentAsFinal(
            @PathVariable Long documentId) {
        documentService.markDocumentAsFinal(documentId);
        return ResponseEntity.ok().build();
    }
}
