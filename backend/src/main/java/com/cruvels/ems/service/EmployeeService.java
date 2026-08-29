package com.cruvels.ems.service;

import com.cruvels.ems.dto.CreateEmployeeRequest;
import com.cruvels.ems.dto.EmployeeResponse;
import com.cruvels.ems.exception.AccessDeniedCustomException;
import com.cruvels.ems.exception.ResourceNotFoundException;
import com.cruvels.ems.model.*;
import com.cruvels.ems.repository.DepartmentRepository;
import com.cruvels.ems.repository.EmployeeRepository;
import com.cruvels.ems.repository.UserRepository;
import com.cruvels.ems.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserProvider currentUserProvider;

    // ADMIN ONLY - creates a login account (User) + a work profile (Employee) together.
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // NEVER save plain text
        user.setRole(request.getRole());
        userRepository.save(user);

        Employee employee = new Employee();
        employee.setUser(user);
        employee.setDesignation(request.getDesignation());
        employee.setJoiningDate(request.getJoiningDate());
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            employee.setDepartment(dept);
        }

        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
            employee.setManager(manager);
        }

        employeeRepository.save(employee);
        return toResponse(employee);
    }

    // Returns the list an Admin sees (everyone) or a Manager sees (only their direct team).
    public List<EmployeeResponse> getVisibleEmployees() {
        Employee currentEmployee = currentUserProvider.getCurrentEmployee();
        User currentUser = currentUserProvider.getCurrentUser();

        List<Employee> employees;
        if (currentUser.getRole() == Role.ADMIN) {
            employees = employeeRepository.findAll();
        } else if (currentUser.getRole() == Role.MANAGER) {
            employees = employeeRepository.findByManagerId(currentEmployee.getId());
        } else {
            // A plain employee only sees themselves through this endpoint
            employees = List.of(currentEmployee);
        }

        return employees.stream().map(this::toResponse).toList();
    }

    public EmployeeResponse getEmployeeById(Long id) {
        Employee target = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        assertCanView(target);
        return toResponse(target);
    }

    // THE KEY AUTHORIZATION CHECK: makes sure a Manager can only view their OWN team,
    // and a regular Employee can only view themselves - enforced here on the backend,
    // not just hidden in the UI.
    private void assertCanView(Employee target) {
        User currentUser = currentUserProvider.getCurrentUser();
        if (currentUser.getRole() == Role.ADMIN) return;

        Employee currentEmployee = currentUserProvider.getCurrentEmployee();

        if (currentUser.getRole() == Role.MANAGER) {
            boolean isSelf = target.getId().equals(currentEmployee.getId());
            boolean isOwnTeamMember = target.getManager() != null
                    && target.getManager().getId().equals(currentEmployee.getId());
            if (!isSelf && !isOwnTeamMember) {
                throw new AccessDeniedCustomException("You can only view your own team");
            }
            return;
        }

        // Role.EMPLOYEE
        if (!target.getId().equals(currentEmployee.getId())) {
            throw new AccessDeniedCustomException("You can only view your own profile");
        }
    }

    public EmployeeResponse deactivateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employee.setEmploymentStatus(EmploymentStatus.INACTIVE);
        employeeRepository.save(employee);
        return toResponse(employee);
    }

    private EmployeeResponse toResponse(Employee e) {
        return new com.cruvels.ems.dto.EmployeeResponse(
                e.getId(),
                e.getUser().getName(),
                e.getUser().getEmail(),
                e.getUser().getRole(),
                e.getDepartment() != null ? e.getDepartment().getName() : null,
                e.getDesignation(),
                e.getJoiningDate(),
                e.getManager() != null ? e.getManager().getUser().getName() : null,
                e.getEmploymentStatus()
        );
    }
}
