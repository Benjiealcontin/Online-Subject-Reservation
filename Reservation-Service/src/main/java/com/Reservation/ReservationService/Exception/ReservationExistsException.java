package com.Reservation.ReservationService.Exception;

public class ReservationExistsException extends RuntimeException{
    public ReservationExistsException(String message) {
        super(message);
    }
}
