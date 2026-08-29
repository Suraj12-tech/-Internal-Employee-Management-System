package com.cruvels.ems.controller;

import com.cruvels.ems.dto.CreateEmployeeRequest;
import com.cruvels.ems.dto.EmployeeResponse;
import com.cruvels.ems.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // Only Admin can create new employees.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.ok(employeeService.createEmployee(request));
    }

    // Any logged-in user can call this - the SERVICE decides how much they see
    // (Admin: everyone, Manager: their team, Employee: just themselves).
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> list() {
        return ResponseEntity.ok(employeeService.getVisibleEmployees());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.deactivateEmployee(id));
    }
}
