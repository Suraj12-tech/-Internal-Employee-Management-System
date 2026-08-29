package com.cruvels.ems.repository;

import com.cruvels.ems.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    List<Attendance> findByEmployeeIdOrderByDateDesc(Long employeeId);
    long countByDate(LocalDate date);
}
