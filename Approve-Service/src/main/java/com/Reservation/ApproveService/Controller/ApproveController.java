package com.Reservation.ApproveService.Controller;


import com.Reservation.ApproveService.Entity.Approve;
import com.Reservation.ApproveService.Exception.ApproveNotFoundException;
import com.Reservation.ApproveService.Exception.ReservationNotFoundException;
import com.Reservation.ApproveService.Service.ApproveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    //Find All Approve
    @GetMapping("/AllApprove")
    public ResponseEntity<?> findAllApprove(){
        try {
            List<Approve> approveList = approveService.getAllApprove();
            return ResponseEntity.ok(approveList);
        }catch (ApproveNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find Approve by ID
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
    @GetMapping("/getByStudentId/{studentId}")
    public ResponseEntity<?> findApproveByStudentId(@PathVariable String studentId){
        try {
            List<Approve> approveList = approveService.getApproveByStudentId(studentId);
            return ResponseEntity.ok(approveList);
        }catch (ApproveNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find Approve by Subject Code
    @GetMapping("/getByStudentId/{studentId}")
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

    //Delete Approve by id
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
