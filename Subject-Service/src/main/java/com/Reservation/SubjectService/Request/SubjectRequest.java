package com.Reservation.SubjectService.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectRequest {
    @NotBlank(message = "Subject code is required")
    private String subjectCode;

    @NotBlank(message = "Subject name is required")
    @Size(min = 2, max = 50, message = "Subject name must be between 2 and 50 characters")
    private String subjectName;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    @Valid
    private InstructorRequest instructor; // The instructor associated with the subject

    @Valid
    private List<@Valid ScheduleRequest> schedule; // List of schedules for the subject

    @NotNull(message = "Available slots must be specified")
    private int availableSlots;
}
