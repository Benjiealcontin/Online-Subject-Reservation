package com.Reservation.SubjectService.Exception;

public class SubjectAlreadyExistsException extends RuntimeException {
    public SubjectAlreadyExistsException(String message) {
        super(message);
    }
}

