package com.cruvels.ems.dto;

import com.cruvels.ems.model.LeaveType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LeaveApplyRequest {
    @NotNull
    private LeaveType leaveType;

    @NotNull
    @FutureOrPresent(message = "Start date cannot be in the past")
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private String reason;
}
