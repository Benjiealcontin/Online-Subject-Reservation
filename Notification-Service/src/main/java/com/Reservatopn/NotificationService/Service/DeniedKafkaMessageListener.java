package com.Reservatopn.NotificationService.Service;

import com.Reservatopn.NotificationService.Service.EmailSender.DeniedEmailSender;
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
public class DeniedKafkaMessageListener {
    private final DeniedEmailSender deniedEmailSender;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeniedKafkaMessageListener(DeniedEmailSender deniedEmailSender) {
        this.deniedEmailSender = deniedEmailSender;
    }

    //Denied Listener 1
    @KafkaListener(topics = "notApprove", groupId = "notApprove-group")
    public void deniedListener1(ConsumerRecord<String, String> record) {
        String key = record.key();
        String value = record.value();
        int partition = record.partition();
        long offset = record.offset();

        try {
            Map<String, Object> approve = extractDeniedData(value);
            deniedEmailSender.notApproveEmailSender(approve.get("email").toString(), approve);

            log.info("Denied Listener 1 received the message with key=[{}] from partition=[{}] with offset=[{}]",
                    key, partition, offset);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    //Denied Listener 2
    @KafkaListener(topics = "notApprove", groupId = "notApprove-group")
    public void deniedListener2(ConsumerRecord<String, String> record) {
        String key = record.key();
        String value = record.value();
        int partition = record.partition();
        long offset = record.offset();

        try {
            Map<String, Object> approve = extractDeniedData(value);
            deniedEmailSender.notApproveEmailSender(approve.get("email").toString(), approve);

            log.info("Denied Listener 2 received the message with key=[{}] from partition=[{}] with offset=[{}]",
                    key, partition, offset);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    //Denied Listener 3
    @KafkaListener(topics = "notApprove", groupId = "notApprove-group")
    public void deniedListener3(ConsumerRecord<String, String> record) {
        String key = record.key();
        String value = record.value();
        int partition = record.partition();
        long offset = record.offset();

        try {
            Map<String, Object> approve = extractDeniedData(value);
            deniedEmailSender.notApproveEmailSender(approve.get("email").toString(), approve);

            log.info("Denied Listener 3 received the message with key=[{}] from partition=[{}] with offset=[{}]",
                    key, partition, offset);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    private Map<String, Object> extractDeniedData(String jsonValue) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(jsonValue);

        Map<String, Object> denied = new HashMap<>();
        denied.put("transactionId", jsonNode.get("transactionId").asText());
        denied.put("subjectCode", jsonNode.get("subjectCode").asText());
        denied.put("studentId", jsonNode.get("studentId").asText());
        denied.put("email", jsonNode.get("email").asText());
        denied.put("day", jsonNode.get("day").asText());
        denied.put("timeSchedule", jsonNode.get("timeSchedule").asText());
        denied.put("location", jsonNode.get("location").asText());
        denied.put("status", jsonNode.get("status").asText());
        denied.put("subjectName", jsonNode.get("subjectName").asText());
        denied.put("familyName", jsonNode.get("lastName").asText());

        return denied;
    }
}
