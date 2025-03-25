package com.example.kafka_connector_poc.producer;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomMessage {
    @NotNull(message = "Train number cannot be null")
    private String trainNumber;  // Changed from id to trainNumber
    private String message;
    private LocalDateTime timestamp;
    private long sequenceNumber;  // Added to track message order
}