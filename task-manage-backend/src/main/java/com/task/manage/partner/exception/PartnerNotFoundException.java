package com.task.manage.partner.exception;

public class PartnerNotFoundException extends RuntimeException {

    public PartnerNotFoundException(String message) {
        super(message);
    }

    public PartnerNotFoundException(Long id) {
        super("Partner not found with id: " + id);
    }
}
