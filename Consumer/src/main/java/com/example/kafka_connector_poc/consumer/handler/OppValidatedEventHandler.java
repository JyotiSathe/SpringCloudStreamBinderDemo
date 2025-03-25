package com.example.kafka_connector_poc.consumer.handler;

import com.example.kafka_connector_poc.consumer.event.OPPValidateEvent;
import com.example.kafka_connector_poc.consumer.model.CustomMessage;
import com.example.kafka_connector_poc.consumer.model.JourneyProfileMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OppValidatedEventHandler {
    private final Logger log = LoggerFactory.getLogger(OppValidatedEventHandler.class);

    private final StreamBridge streamBridge;
    private final JourneyProfileMapper jpMapper;

    @Autowired
    public OppValidatedEventHandler(StreamBridge streamBridge, JourneyProfileMapper jpMapper) {
        this.streamBridge = streamBridge;
        this.jpMapper = jpMapper;
    }

    @EventListener
    @Async("journeyPlanTaskExecutor")
    public void handleJourneyPlanValidatedEvent(OPPValidateEvent event) {
        CustomMessage customMessage = event.customMessage();
        try {
            log.info("Transforming journey plan: {}", customMessage.getTrainNumber());

            JourneyProfileMessage message = jpMapper.mapToJourneyProfile(customMessage);

            streamBridge.send("journeyPlanPublisher-out-0", message);

        } catch (Exception e) {
            log.error("Error transforming journey plan {}: {}", customMessage.getTrainNumber(), e.getMessage(), e);
        }
    }
}
