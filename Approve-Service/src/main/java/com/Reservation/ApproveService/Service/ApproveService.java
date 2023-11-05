package com.Reservation.ApproveService.Service;


import com.Reservation.ApproveService.Dto.MessageResponse;
import com.Reservation.ApproveService.Dto.ReservationDTO;
import com.Reservation.ApproveService.Dto.SubjectDTO;
import com.Reservation.ApproveService.Entity.Approve;
import com.Reservation.ApproveService.Exception.ApproveNotFoundException;
import com.Reservation.ApproveService.Exception.NoAvailableSlotsException;
import com.Reservation.ApproveService.Exception.ReservationNotFoundException;
import com.Reservation.ApproveService.Repository.ApproveRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.net.HttpHeaders;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.BeanUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class ApproveService {
    public final String RESERVATION_URI = "http://Reservation-Service/api/reservation";
    public final String SUBJECT_URI = "http://Subject-Service/api/subject";
    private final ApproveRepository approveRepository;
    private final WebClient.Builder webClientBuilder;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    String SUBJECT_URL = "http://Subject-Service/api/subject";

    public ApproveService(ApproveRepository approveRepository, WebClient.Builder webClientBuilder, KafkaTemplate<String, Object> kafkaTemplate) {
        this.approveRepository = approveRepository;
        this.webClientBuilder = webClientBuilder;
        this.kafkaTemplate = kafkaTemplate;
    }

    //Approve Reservation
    @CircuitBreaker(name = "Approve", fallbackMethod = "ApproveFallback")
    public MessageResponse approveReservation(Long id, String bearerToken) {
        try {
            ReservationDTO reservation = webClientBuilder.build()
                    .get()
                    .uri(RESERVATION_URI + "/getReservation/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(ReservationDTO.class)
                    .block();

            assert reservation != null;
            Approve approve = new Approve();

            BeanUtils.copyProperties(reservation, approve);
            approve.setStatus("Confirmed");
            approve.setApprovedAt(LocalDateTime.now());

            SubjectDTO subjectDTO = webClientBuilder.build()
                    .get()
                    .uri(SUBJECT_URL + "/subjectCode/{subjectCode}", reservation.getSubjectCode())
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(SubjectDTO.class)
                    .block();

            assert subjectDTO != null;
            if (subjectDTO.getAvailableSlots() == 0) {
                throw new NoAvailableSlotsException("No available slots for subject with subject code: " + subjectDTO.getSubjectCode());
            }

            Mono<Void> result = webClientBuilder.build()
                    .delete()
                    .uri(RESERVATION_URI + "/delete/{id}", reservation.getId())
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .toBodilessEntity()
                    .then();
            result.block();

            approveRepository.save(approve);

            approveNotification(approve);

            return new MessageResponse("Approve Successfully.");
        } catch (WebClientResponseException.NotFound e) {
            throw new ReservationNotFoundException(e.getResponseBodyAsString());
        }
    }

    //Not Approve Reservation
    @CircuitBreaker(name = "Approve", fallbackMethod = "ApproveFallback")
    public MessageResponse notApproveReservation(Long id, String bearerToken) {
        try {
            ReservationDTO reservation = webClientBuilder.build()
                    .get()
                    .uri(RESERVATION_URI + "/getReservation/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(ReservationDTO.class)
                    .block();

            assert reservation != null;
            Approve approve = new Approve();

            BeanUtils.copyProperties(reservation, approve);
            approve.setStatus("Denied");
            approve.setApprovedAt(LocalDateTime.now());


            Mono<Void> result = webClientBuilder.build()
                    .delete()
                    .uri(RESERVATION_URI + "/delete/{id}", reservation.getId())
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .toBodilessEntity()
                    .then();
            result.block();

            SubjectDTO subjectDTO = webClientBuilder.build()
                    .get()
                    .uri(SUBJECT_URL + "/subjectCode/{subjectCode}", reservation.getSubjectCode())
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(SubjectDTO.class)
                    .block();

            assert subjectDTO != null;
            if (subjectDTO.getAvailableSlots() == 0) {
                throw new NoAvailableSlotsException("No available slots for subject with subject code: " + subjectDTO.getSubjectCode());
            }

            Mono<Void> result2 = webClientBuilder.build()
                    .put()
                    .uri(SUBJECT_URI + "/slotAddition/{subjectCode}", reservation.getSubjectCode())
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .toBodilessEntity()
                    .then();
            result2.block();

            notApproveNotification(approve);

            approveRepository.save(approve);

            return new MessageResponse("Not Approve Successfully.");
        } catch (NoAvailableSlotsException e) {
            throw new NoAvailableSlotsException(e.getMessage());
        } catch (WebClientResponseException.Conflict e) {
            throw new NoAvailableSlotsException(e.getResponseBodyAsString());
        } catch (WebClientResponseException.NotFound e) {
            throw new ReservationNotFoundException(e.getResponseBodyAsString());
        }
    }

    //Approve Notification
    public void approveNotification(Approve approve) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            String jsonMessage = objectMapper.writeValueAsString(approve);

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("approve", approve.getStudentId(), jsonMessage);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    RecordMetadata metadata = result.getRecordMetadata();

                    log.info("Sent message with key=[{}] and value=[{}] to partition=[{}] with offset=[{}]",
                            approve.getStudentId(), jsonMessage, metadata.partition(), metadata.offset());
                } else {
                    log.error("Unable to send message with key=[{}] and value=[{}] due to: {}", approve.getStudentId(), jsonMessage, ex.getMessage());
                }
            });
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    //Not Approve Notification
    public void notApproveNotification(Approve approve) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            String jsonMessage = objectMapper.writeValueAsString(approve);

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("notApprove", approve.getStudentId(), jsonMessage);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    RecordMetadata metadata = result.getRecordMetadata();

                    log.info("Sent message with key=[{}] and value=[{}] to partition=[{}] with offset=[{}]",
                            approve.getStudentId(), jsonMessage, metadata.partition(), metadata.offset());
                } else {
                    log.error("Unable to send message with key=[{}] and value=[{}] due to: {}", approve.getStudentId(), jsonMessage, ex.getMessage());
                }
            });
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    //Find Approve By ID
    public Optional<Approve> getApproveById(Long id) {
        Optional<Approve> approve = approveRepository.findById(id);

        if (approve.isEmpty()) {
            throw new ApproveNotFoundException("No approve found with Id: " + id);
        }

        return approve;
    }

    //Find All
    public List<Approve> getAllApprove() {
        List<Approve> approveList = approveRepository.findAll();

        if (approveList.isEmpty()) {
            throw new ApproveNotFoundException("No approves found");
        }

        return approveList;
    }


    //Find All Approve
    public List<Approve> getAllApproveByConfirmed() {
        List<Approve> approveList = approveRepository.findAllByStatus("Confirmed");

        if (approveList.isEmpty()) {
            throw new ApproveNotFoundException("No approves found");
        }

        return approveList;
    }

    //Find All Not Approve
    public List<Approve> getAllNotApprove() {
        List<Approve> approveList = approveRepository.findAllByStatus("Denied");

        if (approveList.isEmpty()) {
            throw new ApproveNotFoundException("No denied approves found");
        }

        return approveList;
    }

    //Find All Approve of student by StudentId
    public List<Approve> getApproveByStudentId(String studentId) {
        List<Approve> approveList = approveRepository.findAllByStudentIdAndStatus(studentId, "Confirmed");

        if (approveList.isEmpty()) {
            throw new ApproveNotFoundException("Approve with Student ID " + studentId + " not found.");
        }

        return approveList;
    }

    //Find All Not Approve of student by StudentId
    public List<Approve> getNotApproveByStudentId(String studentId) {
        List<Approve> approveList = approveRepository.findAllByStudentIdAndStatus(studentId, "Denied");

        if (approveList.isEmpty()) {
            throw new ApproveNotFoundException("Not Approve with Student ID " + studentId + " not found.");
        }

        return approveList;
    }

    //Find All Approve of student by subject code
    public List<Approve> getApproveOfStudentBySubjectCode(String subjectCode) {
        List<Approve> approveList = approveRepository.findAllBySubjectCodeAndStatus(subjectCode, "Confirmed");

        if (approveList.isEmpty()) {
            throw new ApproveNotFoundException("Approve with Subject Code " + subjectCode + " not found.");
        }

        return approveList;
    }

    //Find All Not Approve of student by subject code
    public List<Approve> getNotApproveOfStudentBySubjectCode(String subjectCode) {
        List<Approve> approveList = approveRepository.findAllBySubjectCodeAndStatus(subjectCode, "Denied");

        if (approveList.isEmpty()) {
            throw new ApproveNotFoundException("Not Approve with Subject Code " + subjectCode + " not found.");
        }

        return approveList;
    }

    //Delete approve and not approve
    public void deleteApproveById(Long id) {
        if (!approveRepository.existsById(id)) {
            throw new ApproveNotFoundException("Approve with ID " + id + " not found.");
        }
        approveRepository.deleteById(id);
    }

    //Circuit Breaker
    public MessageResponse ReservationFallback(Long id, String bearerToken, Exception e, Throwable throwable) {
        if (e instanceof ReservationNotFoundException) {
            throw new ReservationNotFoundException(e.getMessage());
        } else if (e instanceof NoAvailableSlotsException) {
            throw new NoAvailableSlotsException(e.getMessage());
        } else {
            log.error("Circuit breaker fallback: Unable to create appointment. Error: {}", throwable.getMessage());
            return new MessageResponse("Reservation creation is currently unavailable. Please try again later.");
        }
    }
}
