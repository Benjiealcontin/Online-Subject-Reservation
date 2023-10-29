package com.Reservation.ReservationService.Repository;

import com.Reservation.ReservationService.Entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
