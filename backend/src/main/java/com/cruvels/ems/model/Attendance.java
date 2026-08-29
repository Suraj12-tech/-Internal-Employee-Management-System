package com.cruvels.ems.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

// One row = one employee's attendance for one day.
// The uniqueConstraints below stops the SAME employee from having TWO rows
// for the SAME date - this is what prevents "duplicate check-in" at the database level.
@Entity
@Table(name = "attendance", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "date"})
})
@Getter
@Setter
@NoArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status = AttendanceStatus.PRESENT;
}
