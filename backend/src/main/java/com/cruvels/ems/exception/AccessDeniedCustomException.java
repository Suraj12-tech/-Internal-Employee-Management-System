package com.cruvels.ems.exception;

// Thrown when a logged-in user tries to access/modify data that isn't theirs
// (e.g. a Manager trying to approve leave for someone NOT on their team).
public class AccessDeniedCustomException extends RuntimeException {
    public AccessDeniedCustomException(String message) {
        super(message);
    }
}
