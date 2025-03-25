package com.example.kafka_connector_poc.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomMessageProducer {

    private final StreamBridge streamBridge;

    private final Map<String, Long> sequencePerTrain = new ConcurrentHashMap<>();
    private final List<String> trainNumbers = Arrays.asList("TRAIN_001", "TRAIN_002", "TRAIN_003", "TRAIN_004", "TRAIN_005");
    private final Random random = new Random();
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 1000)
    public void produceCustomMessage() {
        try {
            String trainNumber = trainNumbers.get(random.nextInt(trainNumbers.size()));
            long sequence = sequencePerTrain.computeIfAbsent(trainNumber, k -> 0L);
            sequencePerTrain.put(trainNumber, sequence + 1);

            CustomMessage customMessage = new CustomMessage(
                    trainNumber,
                    "Message for train " + trainNumber,
                    LocalDateTime.now(),
                    sequence
            );

            int partition = Math.abs(trainNumber.hashCode() % 3);  // assuming 3 partitions
            String jsonMessage = objectMapper.writeValueAsString(customMessage);
            log.info("Producing message - Train: {}, Sequence: {}, Expected Partition: {}",
                    trainNumber, sequence, partition);
            streamBridge.send("produceMessage-out-0", jsonMessage);

        } catch (JsonProcessingException e) {
            log.error("Error converting to JSON", e);
        }

    }
}
