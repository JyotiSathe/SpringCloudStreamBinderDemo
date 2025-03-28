package com.example.kafka_connector_poc.consumer.config;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;

/**
 * Interface for mapping between Java objects and their JSON representations.
 */
public interface IMapper {

    /**
     * Serializes the given object to a byte array using the provided root type reference.
     *
     * @param value    The object to be serialized.
     * @param rootType The TypeReference representing the root type for serialization.
     * @param <R>      The type of the root object.
     * @return The byte array representing the serialized object.
     * @throws IOException If an I/O error occurs during serialization.
     */
    <R> byte[] writeValue(Object value, TypeReference<R> rootType) throws IOException;

    /**
     * Deserializes the given byte array to an object of the specified type using the provided type reference.
     *
     * @param content   The byte array to be deserialized.
     * @param valueType The TypeReference representing the type of the object to be deserialized.
     * @param <T>       The type of the object to be deserialized.
     * @return The deserialized object.
     * @throws IOException If an I/O error occurs during deserialization.
     */
    <T> T readValue(byte[] content, TypeReference<T> valueType) throws IOException;

}
