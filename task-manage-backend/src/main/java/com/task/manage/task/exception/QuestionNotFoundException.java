package com.task.manage.task.exception;

public class QuestionNotFoundException extends RuntimeException {

    public QuestionNotFoundException(Long questionId) {
        super("Question not found with id: " + questionId);
    }

    public QuestionNotFoundException(String message) {
        super(message);
    }
}
