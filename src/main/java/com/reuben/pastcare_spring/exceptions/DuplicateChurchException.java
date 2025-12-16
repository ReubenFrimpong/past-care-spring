package com.reuben.pastcare_spring.exceptions;

/**
 * Exception thrown when attempting to create a church with a name that already exists.
 */
public class DuplicateChurchException extends RuntimeException {
    public DuplicateChurchException(String message) {
        super(message);
    }
}
