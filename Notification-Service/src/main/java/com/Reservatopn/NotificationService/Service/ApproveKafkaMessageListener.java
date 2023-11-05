package com.Reservatopn.NotificationService.Service;

import com.Reservatopn.NotificationService.Service.EmailSender.ApproveEmailSender;
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
public class ApproveKafkaMessageListener {
    private final ApproveEmailSender approveEmailSender;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ApproveKafkaMessageListener(ApproveEmailSender approveEmailSender) {
        this.approveEmailSender = approveEmailSender;
    }

    //Approve Listener 1
    @KafkaListener(topics = "approve", groupId = "approve-group")
    public void approveListener1(ConsumerRecord<String, String> record) {
        String key = record.key();
        String value = record.value();
        int partition = record.partition();
        long offset = record.offset();

        try {
            Map<String, Object> approve = extractApproveData(value);
            approveEmailSender.approveEmailSender(approve.get("email").toString(), approve);

            log.info("Approve Listener 1 received the message with key=[{}] from partition=[{}] with offset=[{}]",
                    key, partition, offset);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    //Approve Listener 2
    @KafkaListener(topics = "approve", groupId = "approve-group")
    public void approveListener2(ConsumerRecord<String, String> record) {
        String key = record.key();
        String value = record.value();
        int partition = record.partition();
        long offset = record.offset();

        try {
            Map<String, Object> approve = extractApproveData(value);
            approveEmailSender.approveEmailSender(approve.get("email").toString(), approve);

            log.info("Approve Listener 2 received the message with key=[{}] from partition=[{}] with offset=[{}]",
                    key, partition, offset);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    //Approve Listener 3
    @KafkaListener(topics = "approve", groupId = "approve-group")
    public void approveListener3(ConsumerRecord<String, String> record) {
        String key = record.key();
        String value = record.value();
        int partition = record.partition();
        long offset = record.offset();

        try {
            Map<String, Object> approve = extractApproveData(value);
            approveEmailSender.approveEmailSender(approve.get("email").toString(), approve);

            log.info("Approve Listener 3 received the message with key=[{}] from partition=[{}] with offset=[{}]",
                    key, partition, offset);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    private Map<String, Object> extractApproveData(String jsonValue) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(jsonValue);

        Map<String, Object> approve = new HashMap<>();
        approve.put("transactionId", jsonNode.get("transactionId").asText());
        approve.put("subjectCode", jsonNode.get("subjectCode").asText());
        approve.put("studentId", jsonNode.get("studentId").asText());
        approve.put("email", jsonNode.get("email").asText());
        approve.put("day", jsonNode.get("day").asText());
        approve.put("timeSchedule", jsonNode.get("timeSchedule").asText());
        approve.put("location", jsonNode.get("location").asText());
        approve.put("status", jsonNode.get("status").asText());
        approve.put("subjectName", jsonNode.get("subjectName").asText());
        approve.put("familyName", jsonNode.get("lastName").asText());

        return approve;
    }
}
