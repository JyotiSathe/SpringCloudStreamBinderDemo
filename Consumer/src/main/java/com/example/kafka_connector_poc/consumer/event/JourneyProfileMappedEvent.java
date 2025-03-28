package com.example.kafka_connector_poc.consumer.event;

import com.example.kafka_connector_poc.consumer.model.JourneyProfileMessage;

public record JourneyProfileMappedEvent(JourneyProfileMessage journeyProfileMessage) {
}
