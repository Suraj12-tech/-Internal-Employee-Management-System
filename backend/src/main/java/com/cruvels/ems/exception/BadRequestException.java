package com.cruvels.ems.exception;

// Thrown for business-rule validation failures, e.g. invalid date ranges,
// duplicate attendance, applying leave with endDate before startDate, etc.
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
