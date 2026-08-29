package com.cruvels.ems.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

// This is the "work profile" of a person. Every Employee is linked to exactly
// one User (the login account). Admin/HR does not necessarily have an Employee row,
// but usually will for consistency.
@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One-to-one link to the login account.
    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    private String designation;

    private LocalDate joiningDate;

    // Self-referencing relationship: an Employee's manager is ALSO an Employee.
    // This is how we build the reporting hierarchy (who reports to whom).
    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @Enumerated(EnumType.STRING)
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;
}
