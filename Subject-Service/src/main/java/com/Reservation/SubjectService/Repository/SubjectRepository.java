package com.Reservation.SubjectService.Repository;

import com.Reservation.SubjectService.Entity.Instructor;
import com.Reservation.SubjectService.Entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    boolean existsBySubjectName(String subjectName);
    boolean existsBySubjectCode(String subjectCode);
    Optional<Subject> findBySubjectCode(String subjectCode);
    Optional<Subject> findSubjectByInstructor_Firstname(String firstname);
    Optional<Subject> findSubjectByInstructor_Lastname(String lastname);
    Optional<Subject> findSubjectByInstructor_Expertise(String expertise);
}
