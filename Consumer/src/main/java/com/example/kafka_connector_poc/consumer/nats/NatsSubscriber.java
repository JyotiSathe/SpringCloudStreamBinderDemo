package com.example.kafka_connector_poc.consumer.nats;

import com.example.kafka_connector_poc.consumer.config.IMapper;
import com.example.kafka_connector_poc.consumer.config.ObjectMapperConfiguration;
import com.example.kafka_connector_poc.consumer.handler.JourneyProfileHandler;
import com.example.kafka_connector_poc.consumer.model.JourneyProfileMessage;
import com.example.kafka_connector_poc.consumer.model.ReplyMessage;
import com.example.kafka_connector_poc.consumer.model.RequestMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.spring.boot.autoconfigure.NatsAutoConfiguration;
import jakarta.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({NatsAutoConfiguration.class, ObjectMapperConfiguration.class})
public class NatsSubscriber {

    private final Connection natsConnection;

    private final IMapper mapper;

    private final ValidatorFactory validatorFactory;

    private final JourneyProfileHandler journeyProfileHandler;

    private final TypeReference<RequestMessage<JourneyProfileMessage>> typeReferenceRequestMessageSegIdNoVersion = new TypeReference<>() {
    };

    private final TypeReference<ReplyMessage<String>> typeReferenceReplyMessageSegment = new TypeReference<>() {
    };

    public NatsSubscriber(Connection natsConnection, IMapper mapper, ValidatorFactory validatorFactory, JourneyProfileHandler journeyProfileHandler) {
        this.natsConnection = natsConnection;
        this.mapper = mapper;
        this.validatorFactory = validatorFactory;
        this.journeyProfileHandler = journeyProfileHandler;
    }

    @Bean
    @Qualifier("add.journey.profile")
    public Dispatcher addJourneyProfileDispatcher() {
        return natsConnection.createDispatcher(new NatsRequestReplyHandler<>(
                journeyProfileHandler::addJourneyProfile,
                typeReferenceRequestMessageSegIdNoVersion,
                typeReferenceReplyMessageSegment,
                natsConnection,
                mapper,
                validatorFactory.getValidator()
        )).subscribe("add.journey.profile");
    }
}
