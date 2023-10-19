package com.Reservation.SubjectService.Exception;

public class SubjectCodeAlreadyExistsException extends RuntimeException {
    public SubjectCodeAlreadyExistsException(String message) {
        super(message);
    }
}

