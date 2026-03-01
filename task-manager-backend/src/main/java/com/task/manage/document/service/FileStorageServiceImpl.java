package com.task.manage.document.service;

import com.task.manage.document.exception.FileNotFoundException;
import com.task.manage.document.exception.FileStorageException;
import com.task.manage.document.exception.InvalidFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageServiceImpl(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage location created at: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            log.error("Could not create the directory where the uploaded files will be stored.", ex);
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file, Long taskId, String documentType, Integer version) {
        // Normalize file name
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            // Check if the file's name contains invalid characters
            if (originalFileName.contains("..")) {
                throw new InvalidFileException("Invalid path sequence in filename: " + originalFileName);
            }

            // Create task-specific directory
            Path taskDirectory = this.fileStorageLocation.resolve("task_" + taskId);
            Files.createDirectories(taskDirectory);

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = taskDirectory.resolve(originalFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored successfully: {}", targetLocation);
            return targetLocation.toString();

        } catch (IOException ex) {
            log.error("Could not store file {}. Please try again!", originalFileName, ex);
            throw new FileStorageException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String filePath) {
        try {
            Path path = Paths.get(filePath).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException(filePath);
            }
        } catch (MalformedURLException ex) {
            log.error("File not found: {}", filePath, ex);
            throw new FileNotFoundException("File not found: " + filePath, ex);
        }
    }

    @Override
    public String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return filename.substring(lastIndexOf + 1);
    }

    @Override
    public boolean isPdf(String filename) {
        return "pdf".equalsIgnoreCase(getFileExtension(filename));
    }
}
