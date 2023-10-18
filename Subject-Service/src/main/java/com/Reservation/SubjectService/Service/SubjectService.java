package com.Reservation.SubjectService.Service;

import com.Reservation.SubjectService.Dto.MessageResponse;
import com.Reservation.SubjectService.Entity.Instructor;
import com.Reservation.SubjectService.Entity.Schedule;
import com.Reservation.SubjectService.Entity.Subject;
import com.Reservation.SubjectService.Repository.InstructorRepository;
import com.Reservation.SubjectService.Repository.ScheduleRepository;
import com.Reservation.SubjectService.Repository.SubjectRepository;
import com.Reservation.SubjectService.Request.SubjectRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubjectService {
    private final SubjectRepository subjectRepository;
    private final InstructorRepository instructorRepository;
    private final ScheduleRepository scheduleRepository;

    public SubjectService(SubjectRepository subjectRepository, InstructorRepository instructorRepository, ScheduleRepository scheduleRepository) {
        this.subjectRepository = subjectRepository;
        this.instructorRepository = instructorRepository;
        this.scheduleRepository = scheduleRepository;
    }

    //Create a subject

    public MessageResponse createSubject(SubjectRequest subjectRequest) {
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
    }

}
