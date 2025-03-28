package com.example.kafka_connector_poc.consumer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.nio.charset.StandardCharsets;

/**
 * Reply message containing generic reply parameter
 *
 * @param reply      reply object, null if statusCode is not OK
 * @param statusCode HTTP status code
 * @param message    message in case statusCode is not OK
 * @param <R>
 */
public record ReplyMessage<R>(
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@bodyClass")
        R reply,
        @JsonProperty("statusCode")
        int statusCode,
        @JsonProperty("message")
        String message) {

    /**
     * Generic error message in case of unknown request error
     */
    private final static String GENERIC_ERROR_REPLY = "{\"reply\":null,\"statusCode\":400,\"message\":\"cannot process message\"}";
    private final static String GENERIC_VALIDATION_ERROR = "{\"reply\":null,\"statusCode\":406,\"message\":\"cannot process message\"}";

    /**
     * OK reply
     *
     * @param reply reply argument
     * @param <R>   type of reply message
     * @return
     */
    public static <R> ReplyMessage<R> ok(R reply) {
        return new ReplyMessage<>(reply, 200, null);
    }

    public static <R> ReplyMessage<R> error(int httpStatusCode, String message) {
        return new ReplyMessage<>(null, httpStatusCode, message);
    }

    public static byte[] genericErrorAsBytes() {
        return GENERIC_ERROR_REPLY.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] validationErrorAsBytes() {
        return GENERIC_VALIDATION_ERROR.getBytes(StandardCharsets.UTF_8);
    }

    @JsonIgnore
    public boolean isOk() {
        return statusCode >= 200 && statusCode < 300;
    }

    @JsonIgnore
    public boolean isError() {
        return statusCode >= 400 && statusCode < 600;
    }
}
