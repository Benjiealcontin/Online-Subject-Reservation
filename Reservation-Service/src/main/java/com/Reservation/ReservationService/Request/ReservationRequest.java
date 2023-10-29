package com.Reservation.ReservationService.Request;

import lombok.Data;

@Data
public class ReservationRequest {
    private String subjectCode;
    private String day;
    private String timeSchedule;
}
