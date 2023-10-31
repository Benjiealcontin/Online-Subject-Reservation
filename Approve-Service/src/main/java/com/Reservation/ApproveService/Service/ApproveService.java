package com.Reservation.ApproveService.Service;


import com.Reservation.ApproveService.Dto.MessageResponse;
import com.Reservation.ApproveService.Dto.ReservationDTO;
import com.Reservation.ApproveService.Entity.Approve;
import com.Reservation.ApproveService.Exception.ReservationNotFoundException;
import com.Reservation.ApproveService.Repository.ApproveRepository;
import com.google.common.net.HttpHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
public class ApproveService {
    private final ApproveRepository approveRepository;
    private final WebClient.Builder webClientBuilder;

    public ApproveService(ApproveRepository approveRepository, WebClient.Builder webClientBuilder) {
        this.approveRepository = approveRepository;
        this.webClientBuilder = webClientBuilder;
    }

    public final String RESERVATION_URI = "http://Reservation-Service/api/reservation";
    public final String SUBJECT_URI = "http://Subject-Service/api/subject";

    //Approve Reservation
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

            Mono<Void> result = webClientBuilder.build()
                    .delete()
                    .uri(RESERVATION_URI + "/delete/{id}", reservation.getId())
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .toBodilessEntity()
                    .then();
            result.block();

            Mono<Void> result2 = webClientBuilder.build()
                    .put()
                    .uri(SUBJECT_URI + "/slot/{subjectCode}", reservation.getSubjectCode())
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .toBodilessEntity()
                    .then();
            result2.block();

            approveRepository.save(approve);

            return new MessageResponse("Approve Successfully.");
        } catch (WebClientResponseException.NotFound e) {
            throw new ReservationNotFoundException(e.getResponseBodyAsString());
        }
    }
}
