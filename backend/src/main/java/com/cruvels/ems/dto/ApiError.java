package com.cruvels.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// A consistent shape for every error response the API sends back.
@Getter
@AllArgsConstructor
public class ApiError {
    private LocalDateTime timestamp;
    private int status;
    private String message;
}
