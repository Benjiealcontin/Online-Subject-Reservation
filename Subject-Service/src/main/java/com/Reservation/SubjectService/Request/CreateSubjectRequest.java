package com.Reservation.SubjectService.Request;

import com.Reservation.SubjectService.Dto.InstructorDTO;
import com.Reservation.SubjectService.Dto.ScheduleDTO;
import com.Reservation.SubjectService.Dto.SubjectDTO;
import lombok.Data;

import java.util.List;

@Data
public class CreateSubjectRequest {
    private SubjectDTO subjectDTO;
    private InstructorDTO instructorDTO;
    private List<ScheduleDTO> scheduleDTOs;
}
