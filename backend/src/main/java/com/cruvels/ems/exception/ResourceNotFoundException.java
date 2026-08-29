package com.cruvels.ems.exception;

// Thrown whenever code tries to fetch something (employee, department, leave...)
// by an ID that doesn't exist. Caught centrally by GlobalExceptionHandler -> returns 404.
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
