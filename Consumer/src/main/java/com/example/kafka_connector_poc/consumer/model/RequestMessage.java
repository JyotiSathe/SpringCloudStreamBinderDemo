package com.example.kafka_connector_poc.consumer.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Request message containing generic parameter
 *
 * @param payload
 * @param <T>
 */
public record RequestMessage<T>(
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@bodyClass")
        T payload) {
}
