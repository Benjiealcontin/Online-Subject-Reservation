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
import com.google.common.net.HttpHeaders;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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

        try {
            SubjectDTO reservationDto = webClientBuilder.build()
                    .get()
                    .uri(SUBJECT_URL + "/subjectCode/{subjectCode}", SubjectCode)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(SubjectDTO.class)
                    .block();

            assert reservationDto != null;

            List<ScheduleDTO> scheduleList = reservationDto.getScheduleList();
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
                                .status("Pending")
                                .transactionId(transactionId)
                                .build();

                        reservationRepository.save(reservation);

                        reservationNotification(reservation, userTokenDTO);
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
    public void reservationNotification(Reservation reservation, UserTokenDTO userTokenDTO) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("reservation", userTokenDTO.getSub());
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                RecordMetadata metadata = result.getRecordMetadata();
                log.info("Sent message with key=[{}] and value=[{}] to partition=[{}] with offset=[{}]",
                        userTokenDTO.getSub(), reservation, metadata.partition(), metadata.offset());
            } else {
                log.error("Unable to send message with key=[{}] and value=[{}] due to: {}", userTokenDTO.getSub(), reservation, ex.getMessage());
            }
        });
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
    public void cancelReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ReservationNotFoundException("Reservation with ID " + id + " not found.");
        }
        reservationRepository.deleteById(id);
    }

    public MessageResponse ReservationFallback(ReservationRequest reservationRequest, String bearerToken, UserTokenDTO userTokenDTO, Throwable t) {
        log.warn("Circuit breaker fallback: Unable to create reservation. Error: {}", t.getMessage());
        return new MessageResponse("Reservation service is temporarily unavailable. Please try again later.");
    }
}
