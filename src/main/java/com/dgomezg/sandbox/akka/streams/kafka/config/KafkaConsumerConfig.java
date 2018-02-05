package com.dgomezg.sandbox.akka.streams.kafka.config;

import org.apache.kafka.common.serialization.Serdes;

import java.util.Properties;

public class KafkaConsumerConfig {


    private final Properties consumerConfig;

    public KafkaConsumerConfig() {
        consumerConfig = new Properties();
        consumerConfig.put(APPLICATION_ID_CONFIG_KEY, "akka-kafka-stream-poc");
        consumerConfig.put(BOOTSTRAP_SERVERS_CONFIG_KEY, "localhost:9092");
        consumerConfig.put(DEFAULT_KEY_SERDE_CLASS_CONFIG_KEY, Serdes.String().getClass());
        consumerConfig.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG_KEY, Serdes.String().getClass());
        consumerConfig.put(POLL_MS_CONFIG_KEY, 10);
    }

    public Properties getConsumerConfig() {
        return consumerConfig;
    }

    public static final String APPLICATION_ID_CONFIG_KEY = "application.id";
    public static final String BOOTSTRAP_SERVERS_CONFIG_KEY = "bootstrap.servers";
    public static final String DEFAULT_KEY_SERDE_CLASS_CONFIG_KEY = "default.key.serde";
    public static final String DEFAULT_VALUE_SERDE_CLASS_CONFIG_KEY = "default.value.serde";
    public static final String POLL_MS_CONFIG_KEY = "poll.ms";
}
