package com.Reservation.ApproveService.Controller;


import com.Reservation.ApproveService.Exception.ReservationNotFoundException;
import com.Reservation.ApproveService.Service.ApproveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/approve")
public class ApproveController {
    private final ApproveService approveService;

    public ApproveController(ApproveService approveService) {
        this.approveService = approveService;
    }

    //Approve Reservation
    @PostMapping("/approve-reservation/{id}")
    public ResponseEntity<?> approveReservation(@PathVariable Long id,  @RequestHeader("Authorization") String bearerToken){
        try {
            return ResponseEntity.ok(approveService.approveReservation(id, bearerToken));
        }catch (ReservationNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}
