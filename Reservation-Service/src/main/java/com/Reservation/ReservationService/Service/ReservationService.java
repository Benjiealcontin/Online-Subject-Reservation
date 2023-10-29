package com.Reservation.ReservationService.Service;

import com.Reservation.ReservationService.Dto.MessageResponse;
import com.Reservation.ReservationService.Dto.ReservationDTO;
import com.Reservation.ReservationService.Dto.ScheduleDTO;
import com.Reservation.ReservationService.Entity.Reservation;
import com.Reservation.ReservationService.Exception.NoAvailableSlotsException;
import com.Reservation.ReservationService.Exception.SubjectNotFoundException;
import com.Reservation.ReservationService.Repository.ReservationRepository;
import com.Reservation.ReservationService.Request.ReservationRequest;
import com.google.common.net.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final WebClient.Builder webClientBuilder;
    String SUBJECT_URL = "http://Subject-Service/api/subject";

    public ReservationService(ReservationRepository reservationRepository, WebClient.Builder webClientBuilder) {
        this.reservationRepository = reservationRepository;
        this.webClientBuilder = webClientBuilder;
    }

    //Create a Subject Reservation
    public MessageResponse reserveSubject(ReservationRequest reservationRequest, String bearerToken, String studentId) {
        String SubjectCode = reservationRequest.getSubjectCode();
        String dayToMatch = reservationRequest.getDay();
        String TimeToMatch = reservationRequest.getTimeSchedule();

        try {
            ReservationDTO reservationDto = webClientBuilder.build()
                    .get()
                    .uri(SUBJECT_URL + "/subjectCode/{subjectCode}", SubjectCode)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(ReservationDTO.class)
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

                        Reservation reservation = new Reservation();
                        reservation.setDay(day);
                        reservation.setTimeSchedule(time);
                        reservation.setLocation(schedule.getLocation());
                        reservation.setSubjectCode(SubjectCode);
                        reservation.setStatus("Pending");
                        reservation.setStudentId(studentId);

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
        }catch (WebClientResponseException.Conflict e) {
            throw new NoAvailableSlotsException(e.getResponseBodyAsString());
        } catch (WebClientResponseException.NotFound e) {
            throw new SubjectNotFoundException(e.getResponseBodyAsString());
        }
    }
}
