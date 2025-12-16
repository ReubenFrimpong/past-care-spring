package com.reuben.pastcare_spring.exceptions;

/**
 * Exception thrown when attempting to create a user with an email that already exists.
 */
public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException(String message) {
        super(message);
    }
}
