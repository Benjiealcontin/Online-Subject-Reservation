package com.Reservation.SubjectService.Repository;

import com.Reservation.SubjectService.Entity.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {
}
