package com.task.manage.donor.exception;

public class DonorNotFoundException extends RuntimeException {

    public DonorNotFoundException(String message) {
        super(message);
    }

    public DonorNotFoundException(Long id) {
        super("Donor not found with id: " + id);
    }
}

