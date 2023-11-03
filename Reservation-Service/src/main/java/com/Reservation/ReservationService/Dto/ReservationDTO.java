package com.Reservation.ReservationService.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {
    private String transactionId;
    private String subjectCode;
    private String studentId;
    private String email;
    private String day;
    private String timeSchedule;
    private String location;
    private String status;
}
