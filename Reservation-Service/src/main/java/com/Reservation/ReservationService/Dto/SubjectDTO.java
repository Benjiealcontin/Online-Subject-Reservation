package com.Reservation.ReservationService.Dto;

import lombok.Data;

import java.util.List;

@Data
public class SubjectDTO {
    private Long id;
    private String subjectCode;
    private String subjectName;
    private String description;
    private InstructorDTO instructor;
    private List<ScheduleDTO> scheduleList;
    private int availableSlots;
}
