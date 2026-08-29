package com.cruvels.ems.controller;

import com.cruvels.ems.model.Attendance;
import com.cruvels.ems.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/checkin")
    public ResponseEntity<Attendance> checkIn() {
        return ResponseEntity.ok(attendanceService.checkIn());
    }

    @PostMapping("/checkout")
    public ResponseEntity<Attendance> checkOut() {
        return ResponseEntity.ok(attendanceService.checkOut());
    }

    @GetMapping("/me")
    public ResponseEntity<List<Attendance>> myAttendance() {
        return ResponseEntity.ok(attendanceService.getMyAttendance());
    }

    @GetMapping("/employee/{id}")
    public ResponseEntity<List<Attendance>> employeeAttendance(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getEmployeeAttendance(id));
    }
}
