package com.Reservatopn.NotificationService.Service;

import com.Reservatopn.NotificationService.Service.EmailSender.ApproveEmailSender;
import com.Reservatopn.NotificationService.Service.EmailSender.ReservationEmailSender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ReservationKafkaMessageListener {
    private final ReservationEmailSender reservationEmailSender;
    private final ApproveEmailSender approveEmailSender;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReservationKafkaMessageListener(ReservationEmailSender reservationEmailSender, ApproveEmailSender approveEmailSender) {
        this.reservationEmailSender = reservationEmailSender;
        this.approveEmailSender = approveEmailSender;
    }

    //Reservation Listener 1
    @KafkaListener(topics = "reservation", groupId = "reservation-group")
    public void reservationListener1(ConsumerRecord<String, String> record) {
        String key = record.key();
        String value = record.value();
        int partition = record.partition();
        long offset = record.offset();

        try {
            Map<String, Object> reservation = extractReservationData(value);
            reservationEmailSender.reservationEmailSender(reservation.get("email").toString(), reservation);

            log.info("Reservation Listener 1 received the message with key=[{}] from partition=[{}] with offset=[{}]",
                    key, partition, offset);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    //Reservation Listener 2
    @KafkaListener(topics = "reservation", groupId = "reservation-group")
    public void reservationListener2(ConsumerRecord<String, String> record) {
        String key = record.key();
        String value = record.value();
        int partition = record.partition();
        long offset = record.offset();

        try {
            Map<String, Object> reservation = extractReservationData(value);
            reservationEmailSender.reservationEmailSender(reservation.get("email").toString(), reservation);

            log.info("Reservation Listener 2 received the message with key=[{}] from partition=[{}] with offset=[{}]",
                    key, partition, offset);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    //Reservation Listener 3
    @KafkaListener(topics = "reservation", groupId = "reservation-group")
    public void reservationListener3(ConsumerRecord<String, String> record) {
        String key = record.key();
        String value = record.value();
        int partition = record.partition();
        long offset = record.offset();

        try {
            Map<String, Object> reservation = extractReservationData(value);
            reservationEmailSender.reservationEmailSender(reservation.get("email").toString(), reservation);

            log.info("Reservation Listener 3 received the message with key=[{}] from partition=[{}] with offset=[{}]",
                    key, partition, offset);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    private Map<String, Object> extractReservationData(String jsonValue) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(jsonValue);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("transactionId", jsonNode.get("transactionId").asText());
        reservation.put("subjectCode", jsonNode.get("subjectCode").asText());
        reservation.put("studentId", jsonNode.get("studentId").asText());
        reservation.put("email", jsonNode.get("email").asText());
        reservation.put("day", jsonNode.get("day").asText());
        reservation.put("timeSchedule", jsonNode.get("timeSchedule").asText());
        reservation.put("location", jsonNode.get("location").asText());
        reservation.put("status", jsonNode.get("status").asText());
        reservation.put("subjectName", jsonNode.get("subjectName").asText());
        reservation.put("familyName", jsonNode.get("lastName").asText());

        return reservation;
    }
}
