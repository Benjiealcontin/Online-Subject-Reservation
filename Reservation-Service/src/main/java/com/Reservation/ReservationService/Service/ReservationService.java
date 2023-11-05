package com.Reservation.ReservationService.Service;

import com.Reservation.ReservationService.Dto.MessageResponse;
import com.Reservation.ReservationService.Dto.ScheduleDTO;
import com.Reservation.ReservationService.Dto.SubjectDTO;
import com.Reservation.ReservationService.Dto.UserTokenDTO;
import com.Reservation.ReservationService.Entity.Reservation;
import com.Reservation.ReservationService.Exception.NoAvailableSlotsException;
import com.Reservation.ReservationService.Exception.ReservationExistsException;
import com.Reservation.ReservationService.Exception.ReservationNotFoundException;
import com.Reservation.ReservationService.Exception.SubjectNotFoundException;
import com.Reservation.ReservationService.Repository.ReservationRepository;
import com.Reservation.ReservationService.Request.ReservationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final WebClient.Builder webClientBuilder;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    String SUBJECT_URL = "http://Subject-Service/api/subject";

    public ReservationService(ReservationRepository reservationRepository, WebClient.Builder webClientBuilder, KafkaTemplate<String, Object> kafkaTemplate) {
        this.reservationRepository = reservationRepository;
        this.webClientBuilder = webClientBuilder;
        this.kafkaTemplate = kafkaTemplate;
    }

    //Create a Subject Reservation
    @CircuitBreaker(name = "Reservation", fallbackMethod = "ReservationFallback")
    public MessageResponse reserveSubject(ReservationRequest reservationRequest, String bearerToken, UserTokenDTO userTokenDTO) {
        String SubjectCode = reservationRequest.getSubjectCode();
        String dayToMatch = reservationRequest.getDay();
        String TimeToMatch = reservationRequest.getTimeSchedule();
        String transactionId = generateTransactionId();

        if (reservationRepository.existsBySubjectCodeAndStudentId(SubjectCode, userTokenDTO.getSub())) {
            throw new ReservationExistsException("You already reserve the Subject.");
        }

        //TODO check if the subject that reserve is already approve or not

        try {
            SubjectDTO subjectDTO = webClientBuilder.build()
                    .get()
                    .uri(SUBJECT_URL + "/subjectCode/{subjectCode}", SubjectCode)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(SubjectDTO.class)
                    .block();

            assert subjectDTO != null;

            if (subjectDTO.getAvailableSlots() == 0) {
                throw new NoAvailableSlotsException("No available slots for subject with subject code: " + subjectDTO.getSubjectCode());
            }

            Mono<Void> result = webClientBuilder.build()
                    .put()
                    .uri(SUBJECT_URL + "/slotReduction/{subjectCode}", subjectDTO.getSubjectCode())
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .toBodilessEntity()
                    .then();
            result.block();

            List<ScheduleDTO> scheduleList = subjectDTO.getScheduleList();
            boolean foundMatchingDay = false; // Flag to track if any matching day was found
            boolean foundMatchingTime = false; // Flag to track if any matching time was found

            for (ScheduleDTO schedule : scheduleList) {
                String day = schedule.getDay();
                String time = schedule.getTimeSchedule();

                if (dayToMatch.equals(day)) {
                    if (TimeToMatch.equals(time)) {
                        Reservation reservation = Reservation.builder()
                                .day(day)
                                .timeSchedule(time)
                                .location(schedule.getLocation())
                                .subjectCode(SubjectCode)
                                .studentId(userTokenDTO.getSub())
                                .email(userTokenDTO.getEmail())
                                .firstName(userTokenDTO.getGivenName())
                                .lastName(userTokenDTO.getFamilyName())
                                .subjectName(subjectDTO.getSubjectName())
                                .status("Pending")
                                .transactionId(transactionId)
                                .build();

                        reservationNotification(reservation);

                        reservationRepository.save(reservation);
                        foundMatchingTime = true;
                    }
                    foundMatchingDay = true;
                }
            }

            if (!foundMatchingDay) {
                // No matching day was found in the scheduleList
                throw new SubjectNotFoundException("No matching day found.");
            }

            if (!foundMatchingTime) {
                // No matching day was found in the scheduleList
                throw new SubjectNotFoundException("No matching time found.");
            }

            return new MessageResponse("Reservation Successfully!");
        } catch (NoAvailableSlotsException e) {
            throw new NoAvailableSlotsException(e.getMessage());
        } catch (WebClientResponseException.Conflict e) {
            throw new NoAvailableSlotsException(e.getResponseBodyAsString());
        } catch (WebClientResponseException.ServiceUnavailable e) {
            throw new NoAvailableSlotsException("Subject Service is not available.");
        } catch (WebClientResponseException.NotFound e) {
            throw new SubjectNotFoundException(e.getResponseBodyAsString());
        }
    }

    //Transaction ID Generator
    private String generateTransactionId() {
        return UUID.randomUUID().toString();
    }

    //Reservation Notification
    public void reservationNotification(Reservation reservation) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            String jsonMessage = objectMapper.writeValueAsString(reservation);

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("reservation", reservation.getStudentId(), jsonMessage);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    RecordMetadata metadata = result.getRecordMetadata();

                    log.info("Sent message with key=[{}] and value=[{}] to partition=[{}] with offset=[{}]",
                            reservation.getStudentId(), jsonMessage, metadata.partition(), metadata.offset());
                } else {
                    log.error("Unable to send message with key=[{}] and value=[{}] due to: {}", reservation.getStudentId(), jsonMessage, ex.getMessage());
                }
            });
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    //Find By ID
    public Optional<Reservation> getReservationById(Long id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);

        if (reservation.isEmpty()) {
            throw new ReservationNotFoundException("No reservations found with Id: " + id);
        }

        return reservation;
    }

    //Find All Reservation
    public List<Reservation> getAllReservation() {
        List<Reservation> reservations = reservationRepository.findAll();

        if (reservations.isEmpty()) {
            throw new ReservationNotFoundException("No reservations found");
        }

        return reservations;
    }

    //Find All Reservation by Student ID
    public List<Reservation> getAllReservationByStudentId(String studentId) {
        List<Reservation> reservations = reservationRepository.findAllByStudentId(studentId);

        if (reservations.isEmpty()) {
            throw new ReservationNotFoundException("No reservations found with Id: " + studentId);
        }

        return reservations;
    }

    //Find All Reservation by Status
    public List<Reservation> getAllReservationByStatus(String status) {

        List<Reservation> reservations = reservationRepository.findAllByStatus(status);

        if (reservations.isEmpty()) {
            throw new ReservationNotFoundException("No reservations found with Status: " + status);
        }

        return reservations;

    }

    //Delete Reservation
    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ReservationNotFoundException("Reservation with ID " + id + " not found.");
        }
        reservationRepository.deleteById(id);
    }

    //Delete the reserve subject of student
    public void cancelReservation(Long id, UserTokenDTO userTokenDTO) {
        Reservation reservation = reservationRepository.findByIdAndStudentId(id, userTokenDTO.getSub());
        if (reservation == null) {
            throw new ReservationNotFoundException("Reservation not found with id: " + id + " and studentId: " + userTokenDTO.getSub());
        }

        reservationRepository.deleteById(id);
    }

    //Circuit Breaker
    public MessageResponse ReservationFallback(ReservationRequest reservationRequest, String bearerToken, UserTokenDTO userTokenDTO, Exception e, Throwable throwable) {
        if (e instanceof ReservationExistsException) {
            throw new ReservationExistsException(e.getMessage());
        } else if (e instanceof NoAvailableSlotsException) {
            throw new NoAvailableSlotsException(e.getMessage());
        } else {
            log.error("Circuit breaker fallback: Unable to create appointment. Error: {}", throwable.getMessage());
            return new MessageResponse("Reservation creation is currently unavailable. Please try again later.");
        }
    }
}
