package com.Reservatopn.NotificationService.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ReservationKafkaMessageListener {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "reservation", groupId = "reservation-group")
    public void reservationListener1(ConsumerRecord<String, String> record){
        String key = record.key();
        String value = record.value();
        int partition = record.partition();
        long offset = record.offset();

        try{
            JsonNode jsonNode = objectMapper.readTree(value);

            log.info("Reservation Listener 1 received the message with key=[{}]from partition=[{}] with offset=[{}]",
                    key, partition, offset);
        }catch (JsonProcessingException e){
            log.error(e.getMessage());
        }
    }
}
