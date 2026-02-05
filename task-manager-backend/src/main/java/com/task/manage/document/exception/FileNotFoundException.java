package com.task.manage.document.exception;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException(String filePath) {
        super("File not found: " + filePath);
    }

    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
