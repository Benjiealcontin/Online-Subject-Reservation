package com.Reservation.ApproveService.Repository;

import com.Reservation.ApproveService.Entity.Approve;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApproveRepository extends JpaRepository<Approve, Long> {
    List<Approve> findAllByStudentId(String studentId);
    List<Approve> findAllBySubjectCode(String subjectCode);
    List<Approve> findAllByStudentIdAndStatus(String studentId, String status);
    List<Approve> findAllBySubjectCodeAndStatus(String subjectCode, String status);
    List<Approve> findAllByStatus(String status);
}
