package com.cruvels.ems.service;

import com.cruvels.ems.dto.LeaveApplyRequest;
import com.cruvels.ems.exception.AccessDeniedCustomException;
import com.cruvels.ems.exception.BadRequestException;
import com.cruvels.ems.exception.ResourceNotFoundException;
import com.cruvels.ems.model.*;
import com.cruvels.ems.repository.EmployeeRepository;
import com.cruvels.ems.repository.LeaveRequestRepository;
import com.cruvels.ems.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentUserProvider currentUserProvider;

    public LeaveRequest applyLeave(LeaveApplyRequest request) {
        // Business rule validation: end date cannot be before start date.
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        Employee employee = currentUserProvider.getCurrentEmployee();

        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(employee);
        leave.setLeaveType(request.getLeaveType());
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setReason(request.getReason());
        leave.setStatus(LeaveStatus.PENDING);

        return leaveRequestRepository.save(leave);
    }

    public List<LeaveRequest> getMyLeaves() {
        Employee employee = currentUserProvider.getCurrentEmployee();
        return leaveRequestRepository.findByEmployeeIdOrderByAppliedAtDesc(employee.getId());
    }

    // Manager sees only their team's leave requests
    public List<LeaveRequest> getTeamLeaves() {
        Employee manager = currentUserProvider.getCurrentEmployee();
        List<Long> teamIds = employeeRepository.findByManagerId(manager.getId())
                .stream().map(Employee::getId).toList();
        return leaveRequestRepository.findByEmployeeIdInOrderByAppliedAtDesc(teamIds);
    }

    // Admin sees everything
    public List<LeaveRequest> getAllLeaves() {
        List<Long> allIds = employeeRepository.findAll().stream().map(Employee::getId).toList();
        return leaveRequestRepository.findByEmployeeIdInOrderByAppliedAtDesc(allIds);
    }

    public LeaveRequest reviewLeave(Long leaveId, LeaveStatus decision) {
        if (decision != LeaveStatus.APPROVED && decision != LeaveStatus.REJECTED) {
            throw new BadRequestException("Decision must be APPROVED or REJECTED");
        }

        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("This leave request has already been reviewed");
        }

        Employee reviewer = currentUserProvider.getCurrentEmployee();
        User currentUser = currentUserProvider.getCurrentUser();

        // AUTHORIZATION: a Manager can only approve/reject requests from THEIR OWN team.
        // Admin can review anyone's request.
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isDirectManager = leave.getEmployee().getManager() != null
                && leave.getEmployee().getManager().getId().equals(reviewer.getId());

        if (!isAdmin && !isDirectManager) {
            throw new AccessDeniedCustomException("You can only review leave requests from your own team");
        }

        leave.setStatus(decision);
        leave.setReviewedBy(reviewer);
        leave.setReviewedAt(LocalDateTime.now());

        return leaveRequestRepository.save(leave);
    }
}
