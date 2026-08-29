package com.cruvels.ems.repository;

import com.cruvels.ems.model.Employee;
import com.cruvels.ems.model.EmploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUserId(Long userId);
    List<Employee> findByManagerId(Long managerId);
    List<Employee> findByDepartmentId(Long departmentId);
    List<Employee> findByEmploymentStatus(EmploymentStatus status);
    long countByEmploymentStatus(EmploymentStatus status);
}
