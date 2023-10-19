package com.Reservation.SubjectService.Request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Time;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {
    @NotNull(message = "Day is required")
    private String day; // Day of the week (e.g., "Monday", "Tuesday")

    @NotNull(message = "Time is required")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private Time time; // The scheduled time

    @NotNull(message = "Location is required")
    @Size(min = 1, max = 255, message = "Location must be between 1 and 255 characters")
    private String location; // The location of the event
}
