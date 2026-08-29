package com.cruvels.ems;

import com.cruvels.ems.dto.LeaveApplyRequest;
import com.cruvels.ems.exception.BadRequestException;
import com.cruvels.ems.model.*;
import com.cruvels.ems.repository.EmployeeRepository;
import com.cruvels.ems.repository.LeaveRequestRepository;
import com.cruvels.ems.security.CurrentUserProvider;
import com.cruvels.ems.service.LeaveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// These are unit tests for LeaveService using Mockito to "fake" the database layer,
// so we can test business logic in isolation, fast, without needing a real MySQL connection.
@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock private LeaveRequestRepository leaveRequestRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private CurrentUserProvider currentUserProvider;

    @InjectMocks private LeaveService leaveService;

    private Employee employee;

    @BeforeEach
    void setup() {
        User user = new User();
        user.setId(1L);
        user.setName("Priya Employee");
        user.setRole(Role.EMPLOYEE);

        employee = new Employee();
        employee.setId(10L);
        employee.setUser(user);
    }

    // TEST 1 (validation / failure case): applying leave with endDate before startDate
    // must be rejected before it ever reaches the database.
    @Test
    void applyLeave_shouldReject_whenEndDateBeforeStartDate() {
        LeaveApplyRequest request = new LeaveApplyRequest();
        request.setLeaveType(LeaveType.CASUAL);
        request.setStartDate(LocalDate.now().plusDays(5));
        request.setEndDate(LocalDate.now().plusDays(2)); // invalid: before start date
        request.setReason("Testing invalid range");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leaveService.applyLeave(request));

        assertEquals("End date cannot be before start date", ex.getMessage());
        // Confirm it never even tried to save - proves validation happens BEFORE persistence.
        verify(leaveRequestRepository, never()).save(any());
    }

    // TEST 2 (success case): a valid leave request should be saved with PENDING status.
    @Test
    void applyLeave_shouldSucceed_withValidDateRange() {
        when(currentUserProvider.getCurrentEmployee()).thenReturn(employee);
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LeaveApplyRequest request = new LeaveApplyRequest();
        request.setLeaveType(LeaveType.SICK);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(1));
        request.setReason("Fever");

        LeaveRequest result = leaveService.applyLeave(request);

        assertEquals(LeaveStatus.PENDING, result.getStatus());
        assertEquals(employee, result.getEmployee());
    }

    // TEST 3 (authorization / failure case): a Manager cannot approve a leave request
    // from an employee who is NOT on their team.
    @Test
    void reviewLeave_shouldBeDenied_whenManagerIsNotDirectManager() {
        User managerUser = new User();
        managerUser.setId(2L);
        managerUser.setRole(Role.MANAGER);

        Employee reviewingManager = new Employee();
        reviewingManager.setId(99L); // NOT the actual manager of the leave-requester below

        Employee otherManager = new Employee();
        otherManager.setId(50L);

        Employee requester = new Employee();
        requester.setId(20L);
        requester.setManager(otherManager); // requester actually reports to a DIFFERENT manager

        LeaveRequest leave = new LeaveRequest();
        leave.setId(500L);
        leave.setEmployee(requester);
        leave.setStatus(LeaveStatus.PENDING);

        when(leaveRequestRepository.findById(500L)).thenReturn(java.util.Optional.of(leave));
        when(currentUserProvider.getCurrentEmployee()).thenReturn(reviewingManager);
        when(currentUserProvider.getCurrentUser()).thenReturn(managerUser);

        assertThrows(com.cruvels.ems.exception.AccessDeniedCustomException.class,
                () -> leaveService.reviewLeave(500L, LeaveStatus.APPROVED));
    }
}
