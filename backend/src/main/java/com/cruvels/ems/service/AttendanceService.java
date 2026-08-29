package com.cruvels.ems.service;

import com.cruvels.ems.exception.AccessDeniedCustomException;
import com.cruvels.ems.exception.BadRequestException;
import com.cruvels.ems.exception.ResourceNotFoundException;
import com.cruvels.ems.model.*;
import com.cruvels.ems.repository.AttendanceRepository;
import com.cruvels.ems.repository.EmployeeRepository;
import com.cruvels.ems.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentUserProvider currentUserProvider;

    public Attendance checkIn() {
        Employee employee = currentUserProvider.getCurrentEmployee();
        LocalDate today = LocalDate.now();

        attendanceRepository.findByEmployeeIdAndDate(employee.getId(), today).ifPresent(a -> {
            throw new BadRequestException("You have already checked in today");
        });

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(today);
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setStatus(AttendanceStatus.PRESENT);

        return attendanceRepository.save(attendance);
    }

    public Attendance checkOut() {
        Employee employee = currentUserProvider.getCurrentEmployee();
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employee.getId(), today)
                .orElseThrow(() -> new BadRequestException("You must check in before checking out"));

        if (attendance.getCheckOutTime() != null) {
            throw new BadRequestException("You have already checked out today");
        }

        attendance.setCheckOutTime(LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getMyAttendance() {
        Employee employee = currentUserProvider.getCurrentEmployee();
        return attendanceRepository.findByEmployeeIdOrderByDateDesc(employee.getId());
    }

    public List<Attendance> getEmployeeAttendance(Long employeeId) {
        Employee target = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Employee currentEmployee = currentUserProvider.getCurrentEmployee();
        User currentUser = currentUserProvider.getCurrentUser();

        boolean isSelf = target.getId().equals(currentEmployee.getId());
        boolean isManagerOfTarget = target.getManager() != null
                && target.getManager().getId().equals(currentEmployee.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isSelf && !isManagerOfTarget && !isAdmin) {
            throw new AccessDeniedCustomException("You cannot view this employee's attendance");
        }

        return attendanceRepository.findByEmployeeIdOrderByDateDesc(employeeId);
    }
}
