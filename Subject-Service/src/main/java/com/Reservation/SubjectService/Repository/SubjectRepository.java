package com.Reservation.SubjectService.Repository;

import com.Reservation.SubjectService.Entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
}
