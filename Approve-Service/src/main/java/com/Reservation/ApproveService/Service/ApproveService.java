package com.Reservation.ApproveService.Service;


import com.Reservation.ApproveService.Dto.MessageResponse;
import com.Reservation.ApproveService.Dto.ReservationDTO;
import com.Reservation.ApproveService.Entity.Approve;
import com.Reservation.ApproveService.Exception.ApproveNotFoundException;
import com.Reservation.ApproveService.Exception.ReservationNotFoundException;
import com.Reservation.ApproveService.Repository.ApproveRepository;
import com.google.common.net.HttpHeaders;
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
    private final ApproveRepository approveRepository;
    private final WebClient.Builder webClientBuilder;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ApproveService(ApproveRepository approveRepository, WebClient.Builder webClientBuilder, KafkaTemplate<String, Object> kafkaTemplate) {
        this.approveRepository = approveRepository;
        this.webClientBuilder = webClientBuilder;
        this.kafkaTemplate = kafkaTemplate;
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
            approve.setApprovedAt(LocalDateTime.now());


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

            approveNotification(approve);

            return new MessageResponse("Approve Successfully.");
        } catch (WebClientResponseException.NotFound e) {
            throw new ReservationNotFoundException(e.getResponseBodyAsString());
        }
    }

    //Not Approve Reservation
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

            Mono<Void> result2 = webClientBuilder.build()
                    .put()
                    .uri(SUBJECT_URI + "/slot/{subjectCode}", reservation.getSubjectCode())
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .toBodilessEntity()
                    .then();
            result2.block();

            approveRepository.save(approve);

            approveNotification(approve);

            return new MessageResponse("Approve Successfully.");
        } catch (WebClientResponseException.NotFound e) {
            throw new ReservationNotFoundException(e.getResponseBodyAsString());
        }
    }

    //Approve Notification
    public void approveNotification(Approve approve) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("reservation", approve.getStudentId());
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                RecordMetadata metadata = result.getRecordMetadata();
                log.info("Sent message with key=[{}] and value=[{}] to partition=[{}] with offset=[{}]",
                        approve.getStudentId(), approve, metadata.partition(), metadata.offset());
            } else {
                log.error("Unable to send message with key=[{}] and value=[{}] due to: {}", approve.getStudentId(), approve, ex.getMessage());
            }
        });
    }

    //Find Approve By ID
    public Optional<Approve> getApproveById(Long id){
        Optional<Approve> approve = approveRepository.findById(id);

        if (approve.isEmpty()) {
            throw new ApproveNotFoundException("No approve found with Id: " + id);
        }

        return approve;
    }

    //Find All Approve
    public List<Approve> getAllApprove(){
        List<Approve> approveList = approveRepository.findAll();

        if (approveList.isEmpty()) {
            throw new ApproveNotFoundException("No approves found");
        }

        return approveList;
    }

    //Find All Approve of student by StudentId
    public List<Approve> getApproveByStudentId(String studentId){
        List<Approve> approveList = approveRepository.findAllByStudentId(studentId);

        if (approveList.isEmpty()) {
            throw new ApproveNotFoundException("Approve with Student ID " + studentId + " not found.");
        }

        return  approveList;
    }

    //Find All Approve of student by subject code
    public List<Approve> getApproveOfStudentBySubjectCode(String subjectCode){
        List<Approve> approveList = approveRepository.findAllBySubjectCode(subjectCode);

        if (approveList.isEmpty()) {
            throw new ApproveNotFoundException("Approve with Subject Code " + subjectCode + " not found.");
        }

        return  approveList;
    }

    //Delete approve
    public void deleteApproveById(Long id){
        if (!approveRepository.existsById(id)) {
            throw new ApproveNotFoundException("Approve with ID " + id + " not found.");
        }
        approveRepository.deleteById(id);
    }
}
