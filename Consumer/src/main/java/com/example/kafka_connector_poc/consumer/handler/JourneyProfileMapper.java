package com.example.kafka_connector_poc.consumer.handler;

import com.example.kafka_connector_poc.consumer.model.CustomMessage;
import com.example.kafka_connector_poc.consumer.model.JourneyProfileMessage;
import org.springframework.stereotype.Component;

@Component
public class JourneyProfileMapper {

    public JourneyProfileMessage mapToJourneyProfile(CustomMessage customMessage) {
        JourneyProfileMessage jp = new JourneyProfileMessage();
        jp.setTrainNumber(customMessage.getTrainNumber());
        jp.setMessage(customMessage.getMessage());
        jp.setTimestamp(customMessage.getTimestamp());
        return jp;
    }
}
