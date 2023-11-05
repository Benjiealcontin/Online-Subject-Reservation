package com.Reservation.ApproveService.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Approve {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String transactionId;
    private String subjectCode;
    private String studentId;
    private String subjectName;
    private String firstName;
    private String lastName;
    private String email;
    private String day;
    private String timeSchedule;
    private String location;
    private String status;
    @CreationTimestamp
    @Column(name = "approved_at", nullable = false, updatable = false)
    private LocalDateTime approvedAt;
}
