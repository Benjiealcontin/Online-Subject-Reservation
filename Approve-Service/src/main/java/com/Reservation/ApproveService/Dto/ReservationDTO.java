package com.Reservation.ApproveService.Dto;

import lombok.Data;

@Data
public class ReservationDTO {
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
}
