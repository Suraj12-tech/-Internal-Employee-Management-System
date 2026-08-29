package com.cruvels.ems.controller;

import com.cruvels.ems.model.Department;
import com.cruvels.ems.repository.DepartmentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    @GetMapping
    public ResponseEntity<List<Department>> list() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Department> create(@Valid @RequestBody Department department) {
        return ResponseEntity.ok(departmentRepository.save(department));
    }
}
