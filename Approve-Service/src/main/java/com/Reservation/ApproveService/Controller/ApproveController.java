package com.Reservation.ApproveService.Controller;


import com.Reservation.ApproveService.Dto.UserTokenDTO;
import com.Reservation.ApproveService.Entity.Approve;
import com.Reservation.ApproveService.Exception.ApproveNotFoundException;
import com.Reservation.ApproveService.Exception.ReservationNotFoundException;
import com.Reservation.ApproveService.Service.ApproveService;
import com.Reservation.ApproveService.Service.TokenDecodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/approve")
public class ApproveController {
    private final ApproveService approveService;
    private final TokenDecodeService tokenDecodeService;

    public ApproveController(ApproveService approveService, TokenDecodeService tokenDecodeService) {
        this.approveService = approveService;
        this.tokenDecodeService = tokenDecodeService;
    }

    //Approve Reservation
    @PostMapping("/approve-reservation/{id}")
    public ResponseEntity<?> approveReservation(@PathVariable Long id,
                                                @RequestHeader("Authorization") String bearerToken){
        try {
            return ResponseEntity.ok(approveService.approveReservation(id, bearerToken));
        }catch (ReservationNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Not Approve Reservation
    @PostMapping("/not-approve-reservation/{id}")
    public ResponseEntity<?> notApproveReservation(@PathVariable Long id,  @RequestHeader("Authorization") String bearerToken){
        try {
            return ResponseEntity.ok(approveService.notApproveReservation(id, bearerToken));
        }catch (ReservationNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find All Approve
    @GetMapping("/all")
    public ResponseEntity<?> findAllApproveAndNotApprove(){
        try {
            List<Approve> approveList = approveService.getAllApprove();
            return ResponseEntity.ok(approveList);
        }catch (ApproveNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find All Approve
    @GetMapping("/allConfirmed")
    public ResponseEntity<?> findAllApprove(){
        try {
            List<Approve> approveList = approveService.getAllApproveByConfirmed();
            return ResponseEntity.ok(approveList);
        }catch (ApproveNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find All Approve
    @GetMapping("/allDenied")
    public ResponseEntity<?> findAllNotApprove(){
        try {
            List<Approve> approveList = approveService.getAllNotApprove();
            return ResponseEntity.ok(approveList);
        }catch (ApproveNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find Approve and Not Approve by ID
    @GetMapping("/getById/{id}")
    public ResponseEntity<?> findApproveById(@PathVariable Long id){
        try {
            Optional<Approve> approveList = approveService.getApproveById(id);
            return ResponseEntity.ok(approveList);
        }catch (ApproveNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find Approve by Student ID
    @GetMapping("/getApproveOfStudent")
    public ResponseEntity<?> findApproveByStudentId(@RequestHeader("Authorization") String bearerToken){
        try {
            String token = tokenDecodeService.extractToken(bearerToken);
            UserTokenDTO userTokenDTO = tokenDecodeService.decodeToken(token);

            List<Approve> approveList = approveService.getApproveByStudentId(userTokenDTO.getSub());
            return ResponseEntity.ok(approveList);
        }catch (ApproveNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find Not Approve by Student ID
    @GetMapping("/getNotApproveOfStudent")
    public ResponseEntity<?> findNotApproveByStudentId(@RequestHeader("Authorization") String bearerToken){
        try {
            String token = tokenDecodeService.extractToken(bearerToken);
            UserTokenDTO userTokenDTO = tokenDecodeService.decodeToken(token);

            List<Approve> approveList = approveService.getNotApproveByStudentId(userTokenDTO.getSub());
            return ResponseEntity.ok(approveList);
        }catch (ApproveNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find All Approve by Subject Code
    @GetMapping("/getApproveBySubjectCode/{subjectCode}")
    public ResponseEntity<?> findApproveBySubjectCode(@PathVariable String subjectCode){
        try {
            List<Approve> approveList = approveService.getApproveOfStudentBySubjectCode(subjectCode);
            return ResponseEntity.ok(approveList);
        }catch (ApproveNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find All Not Approve by Subject Code
    @GetMapping("/getNotApproveBySubjectCode/{subjectCode}")
    public ResponseEntity<?> findNotApproveBySubjectCode(@PathVariable String subjectCode){
        try {
            List<Approve> approveList = approveService.getNotApproveOfStudentBySubjectCode(subjectCode);
            return ResponseEntity.ok(approveList);
        }catch (ApproveNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Delete Approve and Not Approve by id
    @GetMapping("/delete/{id}")
    public ResponseEntity<?> deleteApproveById(@PathVariable Long id){
        try {
            approveService.deleteApproveById(id);
            return ResponseEntity.ok("Delete Successfully.");
        }catch (ApproveNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}
