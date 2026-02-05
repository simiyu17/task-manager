package com.task.manage.task.exception;

public class TaskAlreadyExistsException extends RuntimeException {

    public TaskAlreadyExistsException(String message) {
        super(message);
    }
}
