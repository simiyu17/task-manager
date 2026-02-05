package com.task.manage.task.exception;

public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException(Long reviewId) {
        super("Review not found with id: " + reviewId);
    }

    public ReviewNotFoundException(String message) {
        super(message);
    }
}
