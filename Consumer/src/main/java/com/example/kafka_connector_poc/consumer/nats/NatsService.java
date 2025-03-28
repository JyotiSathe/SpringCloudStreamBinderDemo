package com.example.kafka_connector_poc.consumer.nats;

import com.example.kafka_connector_poc.consumer.config.IMapper;
import com.example.kafka_connector_poc.consumer.model.ReplyMessage;
import com.example.kafka_connector_poc.consumer.model.RequestMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import io.nats.client.Connection;
import io.nats.client.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Request reply or one-way messaging with Nats
 */
@Service
public class NatsService {

    private static final Logger logger = LoggerFactory.getLogger(NatsService.class);

    private final Connection natsConnection;

    private final IMapper objectMapper;

    private final Duration natsCleanupInterval = Duration.ofSeconds(10);

    @Autowired
    public NatsService(Connection natsConnection, IMapper objectMapper) {
        Objects.requireNonNull(natsConnection, "Nats connection cannot be null");
        Objects.requireNonNull(objectMapper, "Object Mapper cannot be null");
        this.natsConnection = natsConnection;
        this.objectMapper = objectMapper;
    }

    /**
     * Request reply message with default timeout
     */
    public <T, R> CompletableFuture<ReplyMessage<T>> request(String subject, RequestMessage<R> payload, TypeReference<RequestMessage<R>> requestClass,
                                                             TypeReference<ReplyMessage<T>> replyClass) {
        return request(subject, payload, requestClass, replyClass, natsCleanupInterval);
    }

    /**
     * Request reply message with specific timeout
     */
    public <T, R> CompletableFuture<ReplyMessage<T>> request(String subject, RequestMessage<R> payload, TypeReference<RequestMessage<R>> requestClass,
                                                             TypeReference<ReplyMessage<T>> replyClass, Duration cleanUpDuration) {

        try {
            return natsConnection.requestWithTimeout(subject, objectMapper.writeValue(payload, requestClass), cleanUpDuration)
                    .thenCompose(response -> processReplyMessage(subject, payload, replyClass, response));

        } catch (IOException | IllegalArgumentException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Process reply message and return CompletableFuture. If the message cannot be deserialized from JSON
     * or the Reply has an error (isError) it returns a failed future.
     *
     * @param subject    NATS subject
     * @param request    request message
     * @param replyClass reply message class
     * @param response   response (can be error)
     * @param <T>        reply message type
     * @param <R>        request message type
     * @return CompletableFuture with ReplyMessage
     */
    private <T, R> CompletableFuture<ReplyMessage<T>> processReplyMessage(String subject, RequestMessage<R> request,
                                                                          TypeReference<ReplyMessage<T>> replyClass,
                                                                          Message response) {

        if (response == null || response.getData() == null || response.getData().length == 0) {
            final String status = response == null ? "null" : response.getStatus().toString();
            logger.error("No response received. Subject: {} Payload:{} Status:{}", subject, request.payload(), status);

            return CompletableFuture.failedFuture(new IOException("No response from NATS received"));
        }

        try {
            final ReplyMessage<T> replyMessage = objectMapper.readValue(response.getData(), replyClass);

            return CompletableFuture.completedFuture(replyMessage);
        } catch (IOException e) {
            logger.error("request", e);

            return CompletableFuture.failedFuture(e);
        }
    }
}
