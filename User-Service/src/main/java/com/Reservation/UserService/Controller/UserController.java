package com.Reservation.UserService.Controller;

import com.Reservation.UserService.Service.TokenDecodeService;
import com.Reservation.UserService.Service.User_Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user")
public class UserController {
    private final User_Service userService;
    private final TokenDecodeService tokenDecodeService;

    public UserController(User_Service userService, TokenDecodeService tokenDecodeService) {
        this.userService = userService;
        this.tokenDecodeService = tokenDecodeService;
    }

    //Get User Info
    @GetMapping("/userinfo")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String bearerToken) {
        try {
            String token = tokenDecodeService.extractToken(bearerToken);

            return ResponseEntity.ok(userService.UserInfo(token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}
