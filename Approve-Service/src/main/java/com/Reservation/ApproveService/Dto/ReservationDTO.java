package com.Reservation.ApproveService.Dto;

import lombok.Data;

@Data
public class ReservationDTO {
    private Long id;
    private String subjectCode;
    private String studentId;
    private String day;
    private String timeSchedule;
    private String location;
    private String status;
}
