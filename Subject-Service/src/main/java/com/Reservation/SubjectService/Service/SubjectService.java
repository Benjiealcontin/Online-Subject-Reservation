package com.Reservation.SubjectService.Service;

import com.Reservation.SubjectService.Dto.InstructorDTO;
import com.Reservation.SubjectService.Dto.MessageResponse;
import com.Reservation.SubjectService.Dto.ScheduleDTO;
import com.Reservation.SubjectService.Dto.SubjectDTO;
import com.Reservation.SubjectService.Entity.Instructor;
import com.Reservation.SubjectService.Entity.Schedule;
import com.Reservation.SubjectService.Entity.Subject;
import com.Reservation.SubjectService.Exception.*;
import com.Reservation.SubjectService.Repository.InstructorRepository;
import com.Reservation.SubjectService.Repository.ScheduleRepository;
import com.Reservation.SubjectService.Repository.SubjectRepository;
import com.Reservation.SubjectService.Request.SubjectRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SubjectService {
    private final SubjectRepository subjectRepository;
    private final InstructorRepository instructorRepository;
    private final ScheduleRepository scheduleRepository;
    private final ModelMapper modelMapper;

    public SubjectService(SubjectRepository subjectRepository, InstructorRepository instructorRepository, ScheduleRepository scheduleRepository, ModelMapper modelMapper) {
        this.subjectRepository = subjectRepository;
        this.instructorRepository = instructorRepository;
        this.scheduleRepository = scheduleRepository;
        this.modelMapper = modelMapper;
    }

    //Create a subject
    @CircuitBreaker(name = "createSubject", fallbackMethod = "createSubjectFallback")
    public MessageResponse createSubject(SubjectRequest subjectRequest) {
        String subjectName = subjectRequest.getSubjectName();
        String subjectCode = subjectRequest.getSubjectCode();

        if (subjectRepository.existsBySubjectName(subjectName)) {
            throw new SubjectAlreadyExistsException("Subject with name " + subjectName + " already exists");
        } else if (subjectRepository.existsBySubjectCode(subjectCode)) {
            throw new SubjectCodeAlreadyExistsException("Subject with code " + subjectCode + " already exists");
        }

        try {
            // Create Subject entity
            Subject subject = new Subject();
            BeanUtils.copyProperties(subjectRequest, subject);

            // Create Instructor entity
            Instructor instructor = new Instructor();
            BeanUtils.copyProperties(subjectRequest.getInstructor(), instructor);
            instructor.setSubject(subject);

            // Create Schedule entities
            List<Schedule> scheduleList = subjectRequest.getSchedule().stream().map(scheduleRequest -> {
                Schedule schedule = new Schedule();
                BeanUtils.copyProperties(scheduleRequest, schedule);
                schedule.setSubject(subject);
                return schedule;
            }).collect(Collectors.toList());

            // Set the schedule list for the subject
            subject.setScheduleList(scheduleList);

            // Save the entities to the database
            instructorRepository.save(instructor);
            scheduleRepository.saveAll(scheduleList);
            subjectRepository.save(subject);

            return new MessageResponse("Subject Created Successfully");
        } catch (Exception e) {
            throw new SubjectCreationException("Subject Creation Failed: " + e.getMessage());
        }
    }

    public MessageResponse createSubjectFallback(SubjectRequest subjectRequest, Throwable t) {
        log.warn("Circuit breaker fallback: Unable to create product. Error: {}", t.getMessage());
        return new MessageResponse("Subject creation is temporarily unavailable. Please try again later.");
    }

    //Find All Subject
    public List<SubjectDTO> getAllSubjects() {
        List<Subject> subjects = subjectRepository.findAll();
        return subjects.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    //Find by Subject Name
    public Optional<SubjectDTO> getSubjectBySubjectCode(String subjectCode) {
        subjectRepository.findBySubjectCode(subjectCode)
                .orElseThrow(() -> new SubjectNotFoundException("Subject not found with Code: " + subjectCode));

        Optional<Subject> subjectOptional = subjectRepository.findBySubjectCode(subjectCode);

        return subjectOptional.map(this::convertToDTO);
    }


    //Delete Subject
    public void deleteSubject(Long subjectId) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new SubjectNotFoundException("Subject with ID " + subjectId + " not found.");
        }
        subjectRepository.deleteById(subjectId);
    }

    //Update Subject
    public void updateSubjectDetailsWithOtherEntities(Long id, SubjectDTO updatedSubjectDTO) {
        // Retrieve the existing subject from the database
        Subject existingSubject = subjectRepository.findById(id)
                .orElseThrow(() -> new SubjectNotFoundException("Subject not found"));

        // Update the fields of the existing Subject entity
        existingSubject.setSubjectCode(updatedSubjectDTO.getSubjectCode());
        existingSubject.setSubjectName(updatedSubjectDTO.getSubjectName());
        existingSubject.setDescription(updatedSubjectDTO.getDescription());
        existingSubject.setAvailableSlots(updatedSubjectDTO.getAvailableSlots());

        // Update the related Instructor entity
        InstructorDTO updatedInstructorDTO = updatedSubjectDTO.getInstructor();
        Instructor existingInstructor = existingSubject.getInstructor();
        existingInstructor.setInstructorId(updatedInstructorDTO.getInstructorId());
        existingInstructor.setFirstname(updatedInstructorDTO.getFirstname());
        existingInstructor.setLastname(updatedInstructorDTO.getLastname());
        existingInstructor.setEmail(updatedInstructorDTO.getEmail());
        existingInstructor.setExpertise(updatedInstructorDTO.getExpertise());

        if(updatedSubjectDTO.getScheduleList()  != null) {
            List<ScheduleDTO> updatedSubjectDTOScheduleList = updatedSubjectDTO.getScheduleList();

            for (int i = 0; i < updatedSubjectDTOScheduleList.size(); i++) {
                ScheduleDTO updatedScheduleDTO = updatedSubjectDTOScheduleList.get(i);
                Schedule existingSchedule = existingSubject.getScheduleList().get(i);

                if (existingSchedule != null) {
                    // Update order item fields from the updatedOrderItemDTO
                    BeanUtils.copyProperties(updatedScheduleDTO, existingSchedule);
                }
            }
        }

        // Save the updated Subject entity
        Subject updatedSubject = subjectRepository.save(existingSubject);

        // Convert and return the updated SubjectDTO
        convertToDTO(updatedSubject);
    }

    private SubjectDTO convertToDTO(Subject subject) {
        return modelMapper.map(subject, SubjectDTO.class);
    }

}
