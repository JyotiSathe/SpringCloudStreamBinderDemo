package com.example.kafka_connector_poc.consumer.handler;

import com.example.kafka_connector_poc.consumer.event.JourneyProfileMappedEvent;
import com.example.kafka_connector_poc.consumer.event.MessageConsumedEvent;
import com.example.kafka_connector_poc.consumer.model.CustomMessage;
import com.example.kafka_connector_poc.consumer.model.JourneyProfileMessage;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MessageConsumedEventHandler {
    private final Logger log = LoggerFactory.getLogger(MessageConsumedEventHandler.class);

    private final JourneyProfileMapper jpMapper;
    private final Validator validator;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public MessageConsumedEventHandler(JourneyProfileMapper jpMapper, Validator validator, ApplicationEventPublisher eventPublisher) {
        this.jpMapper = jpMapper;
        this.validator = validator;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    @Async("journeyPlanTaskExecutor")
    public void handleMessageConsumedEvent(MessageConsumedEvent event) {
        CustomMessage customMessage = event.customMessage();
        try {
            String violations = validateCustomMessage(customMessage);

            if (violations != null && violations.isEmpty()) {
                log.info("Transforming journey plan: {}", customMessage.getTrainNumber());

                JourneyProfileMessage message = jpMapper.mapToJourneyProfile(customMessage);

                eventPublisher.publishEvent(new JourneyProfileMappedEvent(message));
            }

        } catch (Exception e) {
            log.error("Error transforming journey plan {}: {}", customMessage.getTrainNumber(), e.getMessage(), e);
        }
    }

    private String validateCustomMessage(CustomMessage message) {
        Set<ConstraintViolation<CustomMessage>> violations = validator.validate(message);
        if (!violations.isEmpty()) {
            return violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
        }
        return "";
    }
}
