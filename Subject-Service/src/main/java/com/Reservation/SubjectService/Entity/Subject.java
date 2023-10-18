package com.Reservation.SubjectService.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String subjectCode;
    private String subjectName;
    private String description;
    @OneToOne(mappedBy = "subject", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private Instructor instructor;
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    private List<Schedule> scheduleList = new ArrayList<>();
    private int availableSlots;
}
