package com.task.manage.document.exception;

public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(Long documentId) {
        super("Document not found with id: " + documentId);
    }

    public DocumentNotFoundException(String message) {
        super(message);
    }
}
