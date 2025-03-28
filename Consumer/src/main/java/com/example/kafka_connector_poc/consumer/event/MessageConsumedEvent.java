package com.example.kafka_connector_poc.consumer.event;

import com.example.kafka_connector_poc.consumer.model.CustomMessage;

public record MessageConsumedEvent(CustomMessage customMessage) {
}
