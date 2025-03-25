package com.example.kafka_connector_poc.consumer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageValidationMessage {
    MessageValidationMessage.ResponseCode status;

    public enum ResponseCode {

        ACCEPTED("accepted"),
        REJECTED("rejected");
        private final String value;
        private final static Map<String, MessageValidationMessage.ResponseCode> CONSTANTS = new HashMap<String, MessageValidationMessage.ResponseCode>();

        static {
            for (MessageValidationMessage.ResponseCode c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        ResponseCode(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static MessageValidationMessage.ResponseCode fromValue(String value) {
            MessageValidationMessage.ResponseCode constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }
}
