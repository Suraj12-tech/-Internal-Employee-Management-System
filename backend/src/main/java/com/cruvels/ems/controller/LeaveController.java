package com.cruvels.ems.controller;

import com.cruvels.ems.dto.LeaveApplyRequest;
import com.cruvels.ems.model.LeaveRequest;
import com.cruvels.ems.model.LeaveStatus;
import com.cruvels.ems.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    public ResponseEntity<LeaveRequest> apply(@Valid @RequestBody LeaveApplyRequest request) {
        return ResponseEntity.ok(leaveService.applyLeave(request));
    }

    @GetMapping("/me")
    public ResponseEntity<List<LeaveRequest>> myLeaves() {
        return ResponseEntity.ok(leaveService.getMyLeaves());
    }

    @GetMapping("/team")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<List<LeaveRequest>> teamLeaves() {
        return ResponseEntity.ok(leaveService.getTeamLeaves());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveRequest>> allLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<LeaveRequest> approve(@PathVariable Long id) {
        return ResponseEntity.ok(leaveService.reviewLeave(id, LeaveStatus.APPROVED));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<LeaveRequest> reject(@PathVariable Long id) {
        return ResponseEntity.ok(leaveService.reviewLeave(id, LeaveStatus.REJECTED));
    }
}
