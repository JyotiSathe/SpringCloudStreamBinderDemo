package com.example.kafka_connector_poc.consumer.exception;

public class MessageValidationException extends RuntimeException {
    public MessageValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
