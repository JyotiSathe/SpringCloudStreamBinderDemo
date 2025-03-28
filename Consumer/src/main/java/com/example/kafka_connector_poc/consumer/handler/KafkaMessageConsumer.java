package com.example.kafka_connector_poc.consumer.handler;

import com.example.kafka_connector_poc.consumer.event.MessageConsumedEvent;
import com.example.kafka_connector_poc.consumer.model.CustomMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@Slf4j
public class KafkaMessageConsumer {

    private final ObjectMapper objectMapper;

    private final ApplicationEventPublisher publisher;

    @Autowired
    public KafkaMessageConsumer(ObjectMapper objectMapper, ApplicationEventPublisher publisher) {
        this.objectMapper = objectMapper;
        this.publisher = publisher;
    }

    @Bean
    public Consumer<String> consumeMessage() {
        return jsonString -> {
            try {
                CustomMessage customMessage = objectMapper.readValue(jsonString, CustomMessage.class);
                log.info("Received CustomMessage: {}", customMessage);

                publisher.publishEvent(new MessageConsumedEvent(customMessage));
            } catch (JsonProcessingException e) {
                log.error("Error parsing JSON: {}", jsonString, e);
            }
        };
    }

}
