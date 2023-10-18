package com.Reservation.SubjectService.Controller;

import com.Reservation.SubjectService.Dto.MessageResponse;
import com.Reservation.SubjectService.Request.SubjectRequest;
import com.Reservation.SubjectService.Service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/subject")
public class SubjectController {
    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @PostMapping("/create-subject")
    public ResponseEntity<?> createSubject(@RequestBody @Valid SubjectRequest subjectRequest,
                                           BindingResult bindingResult ) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }
            MessageResponse response = subjectService.createSubject(subjectRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

}
