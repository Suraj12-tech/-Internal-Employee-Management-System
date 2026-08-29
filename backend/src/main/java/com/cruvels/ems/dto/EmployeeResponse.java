package com.cruvels.ems.dto;

import com.cruvels.ems.model.EmploymentStatus;
import com.cruvels.ems.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

// What we send back to the frontend for an employee.
// Notice: NO password field here - we never expose sensitive data in API responses.
@Getter
@AllArgsConstructor
public class EmployeeResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private String departmentName;
    private String designation;
    private LocalDate joiningDate;
    private String managerName;
    private EmploymentStatus employmentStatus;
}
