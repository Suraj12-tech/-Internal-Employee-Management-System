package com.cruvels.ems;

import com.cruvels.ems.model.*;
import com.cruvels.ems.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

// Runs automatically once, every time the app starts (only if the database is empty).
// This gives us ready-to-use sample/demo data as required by the assignment,
// without ever needing to touch real personal data.
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // already seeded, don't duplicate data on every restart
        }

        // --- Departments ---
        Department engineering = departmentRepository.save(dept("Engineering", "Builds the product"));
        Department hr = departmentRepository.save(dept("Human Resources", "Manages people operations"));

        // --- Admin ---
        User adminUser = userRepository.save(user("Asha Admin", "admin@cruvels.com", "Admin@123", Role.ADMIN));
        Employee admin = employeeRepository.save(employee(adminUser, hr, "HR Admin", null));

        // --- Manager ---
        User managerUser = userRepository.save(user("Raj Manager", "manager@cruvels.com", "Manager@123", Role.MANAGER));
        Employee manager = employeeRepository.save(employee(managerUser, engineering, "Engineering Manager", null));

        // --- Employees (report to the manager above) ---
        User emp1User = userRepository.save(user("Priya Employee", "priya@cruvels.com", "Employee@123", Role.EMPLOYEE));
        employeeRepository.save(employee(emp1User, engineering, "Software Engineer", manager));

        User emp2User = userRepository.save(user("Vikram Employee", "vikram@cruvels.com", "Employee@123", Role.EMPLOYEE));
        employeeRepository.save(employee(emp2User, engineering, "Software Engineer", manager));

        System.out.println("=== Sample data seeded ===");
        System.out.println("Admin login:   admin@cruvels.com / Admin@123");
        System.out.println("Manager login: manager@cruvels.com / Manager@123");
        System.out.println("Employee login: priya@cruvels.com / Employee@123");
    }

    private Department dept(String name, String description) {
        Department d = new Department();
        d.setName(name);
        d.setDescription(description);
        return d;
    }

    private User user(String name, String email, String rawPassword, Role role) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPassword)); // hash before saving, always
        u.setRole(role);
        return u;
    }

    private Employee employee(User user, Department department, String designation, Employee manager) {
        Employee e = new Employee();
        e.setUser(user);
        e.setDepartment(department);
        e.setDesignation(designation);
        e.setJoiningDate(LocalDate.now().minusMonths(6));
        e.setManager(manager);
        e.setEmploymentStatus(EmploymentStatus.ACTIVE);
        return e;
    }
}
