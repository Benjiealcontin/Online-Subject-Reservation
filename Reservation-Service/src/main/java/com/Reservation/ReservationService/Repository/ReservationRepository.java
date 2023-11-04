package com.Reservation.ReservationService.Repository;

import com.Reservation.ReservationService.Entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsBySubjectCodeAndStudentId(String subjectCode, String studentId);
    Reservation findByIdAndStudentId(Long id, String studentId);
    List<Reservation> findAllByStudentId(String studentId);
    List<Reservation> findAllByStatus(String status);

}
