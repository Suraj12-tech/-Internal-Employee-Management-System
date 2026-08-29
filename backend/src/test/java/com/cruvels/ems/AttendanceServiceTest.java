package com.cruvels.ems;

import com.cruvels.ems.exception.BadRequestException;
import com.cruvels.ems.model.Attendance;
import com.cruvels.ems.model.Employee;
import com.cruvels.ems.model.User;
import com.cruvels.ems.repository.AttendanceRepository;
import com.cruvels.ems.repository.EmployeeRepository;
import com.cruvels.ems.security.CurrentUserProvider;
import com.cruvels.ems.service.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private CurrentUserProvider currentUserProvider;

    @InjectMocks private AttendanceService attendanceService;

    private Employee employee;

    @BeforeEach
    void setup() {
        User user = new User();
        user.setId(1L);
        employee = new Employee();
        employee.setId(10L);
        employee.setUser(user);
    }

    // TEST 4 (edge/error case): checking in TWICE on the same day must be blocked.
    // This is exactly the "handle duplicate/invalid actions" requirement.
    @Test
    void checkIn_shouldFail_whenAlreadyCheckedInToday() {
        when(currentUserProvider.getCurrentEmployee()).thenReturn(employee);
        when(attendanceRepository.findByEmployeeIdAndDate(employee.getId(), LocalDate.now()))
                .thenReturn(Optional.of(new Attendance())); // already exists

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> attendanceService.checkIn());

        assertEquals("You have already checked in today", ex.getMessage());
        verify(attendanceRepository, never()).save(any());
    }

    // TEST 5 (success case): a first-time check-in for the day should succeed and save a record.
    @Test
    void checkIn_shouldSucceed_whenNoRecordExistsForToday() {
        when(currentUserProvider.getCurrentEmployee()).thenReturn(employee);
        when(attendanceRepository.findByEmployeeIdAndDate(employee.getId(), LocalDate.now()))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Attendance result = attendanceService.checkIn();

        assertNotNull(result.getCheckInTime());
        assertEquals(employee, result.getEmployee());
        verify(attendanceRepository, times(1)).save(any());
    }

    // TEST 6 (edge case): checking out WITHOUT having checked in first should fail with a clear message.
    @Test
    void checkOut_shouldFail_whenNoCheckInExistsForToday() {
        when(currentUserProvider.getCurrentEmployee()).thenReturn(employee);
        when(attendanceRepository.findByEmployeeIdAndDate(employee.getId(), LocalDate.now()))
                .thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> attendanceService.checkOut());

        assertEquals("You must check in before checking out", ex.getMessage());
    }
}
