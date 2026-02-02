package com.task.manage.document.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Store a file and return the file path
     */
    String storeFile(MultipartFile file, Long taskId, String documentType, Integer version);

    /**
     * Load a file as Resource for download
     */
    Resource loadFileAsResource(String filePath);

    /**
     * Get the file extension from a filename
     */
    String getFileExtension(String filename);

    /**
     * Check if a file is a PDF
     */
    boolean isPdf(String filename);
}
