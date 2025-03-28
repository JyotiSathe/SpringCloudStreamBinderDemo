package com.example.kafka_connector_poc.consumer.handler;

import com.example.kafka_connector_poc.consumer.model.JourneyProfileMessage;
import com.example.kafka_connector_poc.consumer.model.ReplyMessage;
import com.example.kafka_connector_poc.consumer.model.RequestMessage;
import org.springframework.stereotype.Service;

@Service
public class JourneyProfileHandler {
    public ReplyMessage<String> addJourneyProfile(RequestMessage<JourneyProfileMessage> journeyProfileMessageRequestMessage) {

        final JourneyProfileMessage jp = journeyProfileMessageRequestMessage.payload();

        return ReplyMessage.ok("Journey Profile added for train: " + jp.getTrainNumber());
    }
}
