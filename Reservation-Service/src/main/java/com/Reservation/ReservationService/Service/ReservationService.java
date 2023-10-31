package com.Reservation.ReservationService.Service;

import com.Reservation.ReservationService.Dto.MessageResponse;
import com.Reservation.ReservationService.Dto.SubjectDTO;
import com.Reservation.ReservationService.Dto.ScheduleDTO;
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
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final WebClient.Builder webClientBuilder;
    String SUBJECT_URL = "http://Subject-Service/api/subject";

    public ReservationService(ReservationRepository reservationRepository, WebClient.Builder webClientBuilder) {
        this.reservationRepository = reservationRepository;
        this.webClientBuilder = webClientBuilder;
    }

    //Create a Subject Reservation
    @CircuitBreaker(name = "Reservation", fallbackMethod = "ReservationFallback")
    public MessageResponse reserveSubject(ReservationRequest reservationRequest, String bearerToken, String studentId) {
        String SubjectCode = reservationRequest.getSubjectCode();
        String dayToMatch = reservationRequest.getDay();
        String TimeToMatch = reservationRequest.getTimeSchedule();

        if (reservationRepository.existsBySubjectCodeAndStudentId(SubjectCode, studentId)) {
            throw new ReservationExistsException("User with the provided username and email already exists.");
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

                        Mono<Void> result = webClientBuilder.build()
                                .put()
                                .uri(SUBJECT_URL + "/slot/{id}", reservationDto.getId())
                                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                                .retrieve()
                                .toBodilessEntity()
                                .then();
                        result.block();

                        Reservation reservation = Reservation.builder()
                                .day(day)
                                .timeSchedule(time)
                                .location(schedule.getLocation())
                                .subjectCode(SubjectCode)
                                .studentId(studentId)
                                .status("Pending")
                                .build();

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
        } catch (WebClientResponseException.Conflict e) {
            throw new NoAvailableSlotsException(e.getResponseBodyAsString());
        } catch (WebClientResponseException.ServiceUnavailable e) {
            throw new NoAvailableSlotsException("Subject Service is not available.");
        } catch (WebClientResponseException.NotFound e) {
            throw new SubjectNotFoundException(e.getResponseBodyAsString());
        }
    }

    //Find By ID
    public Optional<Reservation> getReservationById(Long id){
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

    public MessageResponse ReservationFallback(ReservationRequest reservationRequest, String bearerToken, String studentId, Throwable t) {
        log.warn("Circuit breaker fallback: Unable to create reservation. Error: {}", t.getMessage());
        return new MessageResponse("Reservation service is temporarily unavailable. Please try again later.");
    }
}
