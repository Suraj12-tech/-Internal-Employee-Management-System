package com.cruvels.ems.service;

import com.cruvels.ems.model.EmploymentStatus;
import com.cruvels.ems.model.LeaveStatus;
import com.cruvels.ems.model.Role;
import com.cruvels.ems.repository.AttendanceRepository;
import com.cruvels.ems.repository.DepartmentRepository;
import com.cruvels.ems.repository.EmployeeRepository;
import com.cruvels.ems.repository.LeaveRequestRepository;
import com.cruvels.ems.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final DepartmentRepository departmentRepository;
    private final CurrentUserProvider currentUserProvider;

    // Returns a different set of stats depending on who is asking - an Employee gets
    // a small personal summary, Admin gets the full org-wide picture.
    public Map<String, Object> getSummary() {
        Role role = currentUserProvider.getCurrentUser().getRole();
        Map<String, Object> stats = new HashMap<>();

        if (role == Role.ADMIN) {
            stats.put("totalEmployees", employeeRepository.countByEmploymentStatus(EmploymentStatus.ACTIVE));
            stats.put("totalDepartments", departmentRepository.count());
            stats.put("presentToday", attendanceRepository.countByDate(LocalDate.now()));
            stats.put("pendingLeaves", leaveRequestRepository.countByStatus(LeaveStatus.PENDING));
        } else if (role == Role.MANAGER) {
            var employee = currentUserProvider.getCurrentEmployee();
            var team = employeeRepository.findByManagerId(employee.getId());
            stats.put("teamSize", team.size());
            stats.put("pendingLeaves", leaveRequestRepository.countByStatus(LeaveStatus.PENDING));
        } else {
            var employee = currentUserProvider.getCurrentEmployee();
            stats.put("myAttendanceRecords", attendanceRepository.findByEmployeeIdOrderByDateDesc(employee.getId()).size());
            stats.put("myLeaveRequests", leaveRequestRepository.findByEmployeeIdOrderByAppliedAtDesc(employee.getId()).size());
        }

        return stats;
    }
}
