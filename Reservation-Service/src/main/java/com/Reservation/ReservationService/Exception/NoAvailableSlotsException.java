package com.Reservation.ReservationService.Exception;

public class NoAvailableSlotsException extends RuntimeException {
    public NoAvailableSlotsException(String message) {
        super(message);
    }
}

