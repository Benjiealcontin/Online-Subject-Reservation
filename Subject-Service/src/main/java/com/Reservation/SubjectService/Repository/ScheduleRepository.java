package com.Reservation.SubjectService.Repository;

import com.Reservation.SubjectService.Entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
}
