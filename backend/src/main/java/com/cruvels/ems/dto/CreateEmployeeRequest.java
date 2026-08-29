package com.cruvels.ems.dto;

import com.cruvels.ems.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

// Used by Admin when creating a new employee (this also creates the login User).
@Getter
@Setter
public class CreateEmployeeRequest {
    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password; // plain text from the form; we hash it before saving

    @NotNull
    private Role role;

    private Long departmentId;
    private String designation;
    private LocalDate joiningDate;
    private Long managerId; // optional
}
