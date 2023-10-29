package com.Reservation.UserService.Service;

import com.Reservation.UserService.Exception.InvalidTokenException;
import org.springframework.stereotype.Service;

@Service
public class TokenDecodeService {

    public String extractToken(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new InvalidTokenException("Invalid Bearer token");
        }
        return bearerToken.substring(7);
    }
}
