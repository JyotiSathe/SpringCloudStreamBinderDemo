package com.example.kafka_connector_poc.consumer.nats;

import com.example.kafka_connector_poc.consumer.config.IMapper;
import com.example.kafka_connector_poc.consumer.model.ReplyMessage;
import com.example.kafka_connector_poc.consumer.model.RequestMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * NATS message handler for request-reply communication.
 *
 * @param consumer          The Function responsible for generating a ReplyMessage based on the received request.
 * @param messageClass      The TypeReference representing the type of the RequestMessage.
 * @param replyMessageClass The TypeReference representing the type of the ReplyMessage.
 * @param natsConnection    The NATS connection used for publishing the reply.
 * @param mapper            The IMapper used for deserializing and serializing messages.
 * @param validator         The Validator used for validating the message payload.
 * @param <T>               The type of the payload in the RequestMessage.
 * @param <R>               The type of the payload in the ReplyMessage.
 */
public record NatsRequestReplyHandler<T, R>(
        Function<RequestMessage<T>, ReplyMessage<R>> consumer,
        TypeReference<RequestMessage<T>> messageClass,
        TypeReference<ReplyMessage<R>> replyMessageClass,
        Connection natsConnection,
        IMapper mapper,
        Validator validator) implements MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(NatsRequestReplyHandler.class);

    /**
     * Handles an incoming NATS message.
     *
     * @param msg The NATS message to be processed.
     */
    @Override
    public void onMessage(Message msg) {
        Objects.requireNonNull(msg, "Cannot process received message (null)");

        try {

            final RequestMessage<T> message = mapper.readValue(msg.getData(), messageClass);

            if (isNotValid(msg.getReplyTo(), message)) return;

            final ReplyMessage<R> reply = consumer.apply(message);

            if (reply != null) {
                final byte[] serializedBuffer = mapper.writeValue(reply, replyMessageClass);

                natsConnection.publish(msg.getReplyTo(), serializedBuffer);
            } else {
                final String errorMessage = "Failed to process message";

                logger.error(errorMessage);

                final TypeReference<ReplyMessage<String>> validationErrorMessage = new TypeReference<>() {
                };

                final byte[] serializedBuffer = mapper.writeValue(ReplyMessage.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorMessage), validationErrorMessage);

                natsConnection.publish(msg.getReplyTo(), serializedBuffer);
            }
        } catch (IOException | IllegalArgumentException e) {

            logger.error("Cannot process message", e);

            natsConnection.publish(msg.getReplyTo(), ReplyMessage.genericErrorAsBytes());
        }
    }

    /**
     * Validates the payload of the received message using the configured Validator and publishes validation errors if any.
     *
     * @param message The RequestMessage containing the payload to be validated.
     * @return True if the payload is not valid, false otherwise.
     */
    private boolean isNotValid(String replyToTopic, RequestMessage<T> message) {
        if (validator == null) {
            return true;
        }

        try {
            // Validate received message
            final Set<ConstraintViolation<T>> validationConstraints = validator.validate(message.payload());

            if (!validationConstraints.isEmpty()) {

                final String validationError = validationConstraints.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(","));

                logger.error("Message validation errors: {}", validationError);

                final TypeReference<ReplyMessage<String>> validationErrReplyClass = new TypeReference<>() {
                };

                final byte[] serializedBuffer = mapper.writeValue(ReplyMessage.error(HttpStatus.BAD_REQUEST.value(), validationError), validationErrReplyClass);

                natsConnection.publish(replyToTopic, serializedBuffer);

                return true;
            }
        } catch (Exception ex) {
            logger.error("Cannot validate message", ex);

            natsConnection.publish(replyToTopic, ReplyMessage.genericErrorAsBytes());

            return true;
        }

        return false;
    }
}
