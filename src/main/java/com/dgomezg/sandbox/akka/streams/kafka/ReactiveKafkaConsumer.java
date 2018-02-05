package com.dgomezg.sandbox.akka.streams.kafka;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.dgomezg.sandbox.akka.basic.actors.Printer;
import com.dgomezg.sandbox.akka.basic.actors.SlowGreeterWithBackPressure;
import com.dgomezg.sandbox.akka.streams.kafka.config.KafkaConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;


/**
 * Created by dgomezg on 08/08/2017.
 */
public class ReactiveKafkaConsumer {

    private static final String TOPIC = "test";
    private static final String CLIENT_GROUP = "group1";

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveKafkaConsumer.class);

    private static ActorSystem actorSystem;

    public static void main(String[] args) throws Exception {
        createAkkaActorSystem();
        ActorRef printerActor = actorSystem.actorOf(Printer.props(), "printerActor");
        ActorRef slowGreeterActor = actorSystem.actorOf(SlowGreeterWithBackPressure.props("SlowGreeterWithBackPressure", printerActor), "slowGreeterActor");


        Properties kafkaConsumerConfig = new KafkaConsumerConfig().getConsumerConfig();

        ConsumerSettings<String, String> consumerSettings =
                ConsumerSettings
                        .create(actorSystem, Serdes.String().deserializer(), Serdes.String().deserializer())
                        .withGroupId(CLIENT_GROUP);

        for (String property : kafkaConsumerConfig.stringPropertyNames()) {
            consumerSettings = consumerSettings.withProperty(property, kafkaConsumerConfig.getProperty(property));
        }

        Materializer materializer = ActorMaterializer.create(actorSystem);

        Source<SlowGreeterWithBackPressure.Greet, Consumer.Control> sourceConsumer =
                Consumer.plainSource(consumerSettings, Subscriptions.topics(TOPIC))
                        .map(record -> {
                            LOGGER.debug("Retrieved [{}] from topic [{}] with offset {}", record.value(), record.topic(), record.offset());
                            return new SlowGreeterWithBackPressure.Greet(record.value());
                        });

        System.out.println("Starting consumer flow");
        RunnableGraph<Consumer.Control> consumerFlow =
                sourceConsumer.toMat(getSinkActorWithBackPressure(printerActor), Keep.left());
        Consumer.Control finished = consumerFlow.run(materializer);
        System.out.println("Consumer flow started");

        try {
            Thread.currentThread().sleep(10_000);
        } catch (InterruptedException ex) {
        }

        //CompletionStage<Done> done = finished.shutdown();
        try {
            Thread.currentThread().sleep(10_000);
        } catch (InterruptedException ex) {
        }

        //done.thenRun(() -> {actorSystem.shutdown(); actorSystem.awaitTermination();});


    }

    private static Sink<SlowGreeterWithBackPressure.Greet, NotUsed> getSinkActorWithBackPressure(ActorRef actorRef) {
        return Sink.actorRefWithAck(actorRef,
                SlowGreeterWithBackPressure.init,
                SlowGreeterWithBackPressure.ack,
                SlowGreeterWithBackPressure.onComplete,
                Throwable::getMessage);
    }

    private static void createAkkaActorSystem() {
        actorSystem = ActorSystem.create("actors");
    }

}
