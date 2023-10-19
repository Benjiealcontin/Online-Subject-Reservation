package com.Reservation.SubjectService.Dto;

import lombok.Data;

import java.sql.Time;

@Data
public class ScheduleDTO {
    private String day;
    private Time time;
    private String location;
}
