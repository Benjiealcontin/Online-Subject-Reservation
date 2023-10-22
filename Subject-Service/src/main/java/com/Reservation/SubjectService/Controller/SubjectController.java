package com.Reservation.SubjectService.Controller;

import com.Reservation.SubjectService.Dto.MessageResponse;
import com.Reservation.SubjectService.Dto.SubjectDTO;
import com.Reservation.SubjectService.Exception.SubjectAlreadyExistsException;
import com.Reservation.SubjectService.Exception.SubjectCodeAlreadyExistsException;
import com.Reservation.SubjectService.Exception.SubjectNotFoundException;
import com.Reservation.SubjectService.Request.SubjectRequest;
import com.Reservation.SubjectService.Service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/subject")
public class SubjectController {
    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    //
    //  Subject Management
    //

    //Create Subject
    @PostMapping("/create-subject")
    public ResponseEntity<?> createSubject(@RequestBody @Valid SubjectRequest subjectRequest,
                                           BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }
            MessageResponse response = subjectService.createSubject(subjectRequest);
            return ResponseEntity.ok(response);
        } catch (SubjectAlreadyExistsException | SubjectCodeAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("An error occurred: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find All Subjects
    @GetMapping("/AllSubjects")
    public List<SubjectDTO> getAllSubjects() {
        return subjectService.getAllSubjects();
    }

    //Find By Subject Code
    @GetMapping("/subjects/{subjectCode}")
    public ResponseEntity<?> getSubjectBySubjectCode(@PathVariable String subjectCode) {
        try {
            Optional<SubjectDTO> subject = subjectService.getSubjectBySubjectCode(subjectCode);
            return ResponseEntity.ok(subject);
        } catch (SubjectNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Delete Subject
    @DeleteMapping("/delete/{subjectId}")
    public ResponseEntity<?> deleteSubject(@PathVariable Long subjectId) {
        try {
            subjectService.deleteSubject(subjectId);
            return ResponseEntity.ok(new MessageResponse("Subject Delete Successfully!"));
        } catch (SubjectNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Update Subject Details
    @PutMapping("/update/{subjectId}")
    public ResponseEntity<?> updateSubject(@PathVariable Long subjectId, @RequestBody SubjectDTO subjectRequest,
                                           BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }
            subjectService.updateSubjectDetailsWithOtherEntities(subjectId, subjectRequest);
            return ResponseEntity.ok(new MessageResponse("Subject updated Successfully!"));
        } catch (SubjectNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //
    //  Instructor Management
    //


    //Retrieve Instructor by Firstname
    @GetMapping("/instructor/firstname/{firstname}")
    public ResponseEntity<?> findAllInstructorByFirstname(@PathVariable String firstname) {
        try {
            return ResponseEntity.ok(subjectService.getSubjectByInstructorFirstName(firstname));
        } catch (SubjectNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Retrieve Instructor by Lastname
    @GetMapping("/instructor/lastname/{lastname}")
    public ResponseEntity<?> findAllInstructorByLastname(@PathVariable String lastname) {
        try {
            return ResponseEntity.ok(subjectService.getSubjectByInstructorLastname(lastname));
        } catch (SubjectNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Retrieve Instructor by Expertise
    @GetMapping("/instructor/expertise/{expertise}")
    public ResponseEntity<?> findAllInstructorByExpertise(@PathVariable String expertise) {
        try {
            return ResponseEntity.ok(subjectService.getSubjectByInstructorExpertise(expertise));
        } catch (SubjectNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}
