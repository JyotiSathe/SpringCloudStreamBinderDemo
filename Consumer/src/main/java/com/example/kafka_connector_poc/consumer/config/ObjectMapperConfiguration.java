package com.example.kafka_connector_poc.consumer.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Configuration
@Slf4j
public class ObjectMapperConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * Creates an IMapper bean for mapping between Java objects and their JSON representations.
     *
     * @param objectMapper The ObjectMapper instance to use for serialization and deserialization.
     * @return An IMapper instance for custom mapping operations.
     */
    @Bean
    public IMapper apiMapper(ObjectMapper objectMapper) {

        return new IMapper() {
            @Override
            public <R> byte[] writeValue(Object value, TypeReference<R> rootType) throws IOException {
                Objects.requireNonNull(value, "Cannot write null value to underlying bytearray");

                final ObjectWriter writer = objectMapper.writerFor(rootType);

                log.debug(objectMapper.writeValueAsString(value));

                return writer.writeValueAsBytes(value);
            }

            @Override
            public <T> T readValue(byte[] content, TypeReference<T> valueType) throws IOException {
                Objects.requireNonNull(content, "Cannot read null value from underlying bytearray");

                final String asString = new String(content, StandardCharsets.UTF_8);

                log.debug(asString);

                return objectMapper.readValue(asString, valueType);
            }
        };
    }

    @Bean
    public ValidatorFactory validatorFactory() {
        return Validation.buildDefaultValidatorFactory();
    }
}
