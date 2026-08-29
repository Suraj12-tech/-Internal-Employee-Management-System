package com.cruvels.ems.repository;

import com.cruvels.ems.model.LeaveRequest;
import com.cruvels.ems.model.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeIdOrderByAppliedAtDesc(Long employeeId);
    List<LeaveRequest> findByEmployeeIdInOrderByAppliedAtDesc(List<Long> employeeIds);
    long countByStatus(LeaveStatus status);
}
