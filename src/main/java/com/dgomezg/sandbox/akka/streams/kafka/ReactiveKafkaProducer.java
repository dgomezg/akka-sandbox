package com.dgomezg.sandbox.akka.streams.kafka;

import akka.Done;
import akka.actor.ActorSystem;
import akka.kafka.ProducerSettings;
import akka.kafka.javadsl.Producer;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Source;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

public class ReactiveKafkaProducer {

    private static final int UP_TO = 10_000;
    private static final String TOPIC = "test";
    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveKafkaProducer.class);

    public static void main(String[] args) {

        ActorSystem actorContext = ActorSystem.create("actorSystem");

        final ProducerSettings<byte[], String> producerSettings =
                ProducerSettings
                        .create(actorContext, Serdes.ByteArray().serializer(), Serdes.String().serializer())
                        .withBootstrapServers("localhost:32768");

        ActorMaterializer materializer = ActorMaterializer.create(actorContext);
        CompletionStage<Done> done = Source.range(1,UP_TO)
                .map(n -> n.toString())
                .map(i -> {
                    LOGGER.debug("Generating message {} to be publised", i);
                    return new ProducerRecord<byte[], String>(TOPIC, "message " + i);
                })
                .runWith(Producer.plainSink(producerSettings), materializer);

        done.thenRun(actorContext::terminate);
    }
}
