package com.Reservation.ReservationService.Controller;

import com.Reservation.ReservationService.Dto.ReservationDTO;
import com.Reservation.ReservationService.Dto.UserTokenDTO;
import com.Reservation.ReservationService.Entity.Reservation;
import com.Reservation.ReservationService.Exception.NoAvailableSlotsException;
import com.Reservation.ReservationService.Exception.ReservationNotFoundException;
import com.Reservation.ReservationService.Exception.SubjectNotFoundException;
import com.Reservation.ReservationService.Request.ReservationRequest;
import com.Reservation.ReservationService.Service.ReservationService;
import com.Reservation.ReservationService.Service.TokenDecodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/reservation")
public class ReservationController {
    private final ReservationService reservationService;
    private final TokenDecodeService tokenDecodeService;

    public ReservationController(ReservationService reservationService, TokenDecodeService tokenDecodeService) {
        this.reservationService = reservationService;
        this.tokenDecodeService = tokenDecodeService;
    }

    //Subject Reservation
    @PostMapping("/subject")
    public ResponseEntity<?> reserveSubject(@RequestBody ReservationRequest reservationRequest,
                                            @RequestHeader("Authorization") String bearerToken) {
        try {
            String token = tokenDecodeService.extractToken(bearerToken);
            UserTokenDTO userTokenDTO = tokenDecodeService.decodeToken(token);
            String studentId = userTokenDTO.getSub();

            return ResponseEntity.ok(reservationService.reserveSubject(reservationRequest, bearerToken, studentId));
        } catch (SubjectNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (NoAvailableSlotsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find All Reservation
    @GetMapping("/AllReservation")
    public ResponseEntity<?> findAllReservation(){
        try{
            List<Reservation> reservation =  reservationService.getAllReservation();
            return ResponseEntity.ok(reservation);
        } catch (ReservationNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find All Reservation By Student ID
    @GetMapping("/AllReservationByStudentId/{studentId}")
    public ResponseEntity<?> findAllReservationByStudentId(@PathVariable String studentId){
        try{
            List<Reservation> reservation =  reservationService.getAllReservationByStudentId(studentId);
            return ResponseEntity.ok(reservation);
        } catch (ReservationNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find All Reservation By Student Status
    @GetMapping("/AllReservationByStatus/{status}")
    public ResponseEntity<?> findAllReservationByStatus(@PathVariable String status){
        try{
            List<Reservation> reservation =  reservationService.getAllReservationByStatus(status);
            return ResponseEntity.ok(reservation);
        } catch (ReservationNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}
