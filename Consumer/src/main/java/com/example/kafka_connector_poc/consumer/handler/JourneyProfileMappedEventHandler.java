package com.example.kafka_connector_poc.consumer.handler;

import com.example.kafka_connector_poc.consumer.event.JourneyProfileMappedEvent;
import com.example.kafka_connector_poc.consumer.model.JourneyProfileMessage;
import com.example.kafka_connector_poc.consumer.model.MessageValidationMessage;
import com.example.kafka_connector_poc.consumer.model.ReplyMessage;
import com.example.kafka_connector_poc.consumer.model.RequestMessage;
import com.example.kafka_connector_poc.consumer.nats.NatsService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class JourneyProfileMappedEventHandler {

    private final NatsService natsService;
    private final Duration requestCompletionInterval;
    private final StreamBridge streamBridge;

    private final TypeReference<RequestMessage<JourneyProfileMessage>> typeReferenceRequestMessage = new com.fasterxml.jackson.core.type.TypeReference<>() {
    };

    private final TypeReference<ReplyMessage<String>> typeReferenceReplyMessageString = new com.fasterxml.jackson.core.type.TypeReference<>() {
    };

    @Autowired
    public JourneyProfileMappedEventHandler(NatsService natsService, @Value("${nats.completionInterval}") Duration requestCompletionInterval, StreamBridge streamBridge) {
        this.natsService = natsService;
        this.requestCompletionInterval = requestCompletionInterval;
        this.streamBridge = streamBridge;
    }

    /**
     * Listens for JourneyProfileMappedEvent and sends it to NATS
     */
    @EventListener
    public void handleJourneyProfileMappedEvent(JourneyProfileMappedEvent event) throws ExecutionException, InterruptedException, TimeoutException {
        log.info("Publishing to NATS: Train {}", event.journeyProfileMessage().getTrainNumber());

        MessageValidationMessage messageValidationMessage = new MessageValidationMessage();

        final CompletableFuture<Void> responseToAddFuture = natsService.request("add.journey.profile",
                        new RequestMessage<>(event.journeyProfileMessage()),
                        typeReferenceRequestMessage,
                        typeReferenceReplyMessageString)
                .thenAccept(replyMessage -> {
                    if (replyMessage.isOk()) {
                        log.info("add: JourneyProfile response: {}", replyMessage.reply());

                        messageValidationMessage.setStatus(MessageValidationMessage.ResponseCode.ACCEPTED);
                    } else {
                        final String message = String.format("%s: %s", replyMessage.statusCode(), replyMessage.message());

                        messageValidationMessage.setStatus(MessageValidationMessage.ResponseCode.REJECTED);
                        log.error(message);
                        throw new RuntimeException(message);
                    }
                });

        responseToAddFuture.get(requestCompletionInterval.get(ChronoUnit.SECONDS), TimeUnit.SECONDS);

        streamBridge.send("publishToKafka-out-0", messageValidationMessage);
    }
}
