package com.cruvels.ems.model;

// The three roles our system supports.
// Used for both the User table and for @PreAuthorize checks on APIs.
public enum Role {
    EMPLOYEE,
    MANAGER,
    ADMIN
}
