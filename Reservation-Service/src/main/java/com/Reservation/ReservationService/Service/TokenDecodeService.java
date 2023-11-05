package com.Reservation.ReservationService.Service;

import com.Reservation.ReservationService.Dto.UserTokenDTO;
import com.Reservation.ReservationService.Exception.InvalidTokenException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;

@Service
public class TokenDecodeService {
    public String extractToken(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new InvalidTokenException("Invalid Bearer token");
        }
        return bearerToken.substring(7);
    }

    public UserTokenDTO decodeToken(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        String sub = decodedJWT.getSubject();
        String email = decodedJWT.getClaim("email").asString();
        String familyName = decodedJWT.getClaim("family_name").asString();
        String givenName = decodedJWT.getClaim("given_name").asString();
        return new UserTokenDTO(sub, email, givenName, familyName);
    }
}
