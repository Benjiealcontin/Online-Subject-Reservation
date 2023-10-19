package com.Reservation.SubjectService.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Instructor {
    @Id
    private Long id;
    private String instructorId;
    private String firstname;
    private String lastname;
    private String email;
    private String expertise;
    @OneToOne
    @MapsId
    @JoinColumn(name = "subject_id")
    private Subject subject;
}
