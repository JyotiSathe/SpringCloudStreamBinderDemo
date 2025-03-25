package com.example.kafka_connector_poc.consumer.handler;

import com.example.kafka_connector_poc.consumer.event.OPPValidateEvent;
import com.example.kafka_connector_poc.consumer.exception.MessageValidationException;
import com.example.kafka_connector_poc.consumer.model.CustomMessage;
import com.example.kafka_connector_poc.consumer.model.MessageValidationMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MessageValidationProcessor {

    Logger log = LoggerFactory.getLogger(MessageValidationProcessor.class);

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public MessageValidationProcessor(ObjectMapper objectMapper, Validator validator, ApplicationEventPublisher applicationEventPublisher) {
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Bean
    public Function<Message<String>, Message<MessageValidationMessage>> validateMessage() {
        return message -> {
            try {
                CustomMessage customMessage = objectMapper.readValue(
                        message.getPayload(),
                        CustomMessage.class
                );

                String violationMessages = validateCustomMessage(customMessage);

                MessageValidationMessage validationMessage = new MessageValidationMessage();
                if (violationMessages.isBlank()) {
                    validationMessage.setStatus(MessageValidationMessage.ResponseCode.ACCEPTED);
                    // Publish event asynchronously
                    applicationEventPublisher.publishEvent(new OPPValidateEvent(customMessage));
                } else {
                    validationMessage.setStatus(MessageValidationMessage.ResponseCode.REJECTED);
                }

                applicationEventPublisher.publishEvent(new OPPValidateEvent(customMessage));

                return MessageBuilder
                        .withPayload(buildExecutionResponse(validationMessage))
                        .copyHeaders(message.getHeaders())
                        .build();

            } catch (JsonProcessingException e) {
                log.error("Error parsing JSON: {}", message.getPayload(), e);
                throw new MessageValidationException("Invalid JSON format", e);
            }
        };
    }

    private MessageValidationMessage buildExecutionResponse(MessageValidationMessage validationMessage) {
        return validationMessage;
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
