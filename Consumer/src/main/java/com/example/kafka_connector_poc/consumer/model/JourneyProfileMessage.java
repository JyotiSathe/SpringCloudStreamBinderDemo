package com.example.kafka_connector_poc.consumer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class JourneyProfileMessage {
    private String trainNumber;  // Changed from id to trainNumber
    private String message;
    private LocalDateTime timestamp;
}
