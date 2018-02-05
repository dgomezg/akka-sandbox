package com.dgomezg.sandbox.akka.basic.bus;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.dgomezg.sandbox.akka.basic.actors.Printer;
import com.dgomezg.sandbox.akka.basic.actors.SlowGreeterWithBackPressure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusAckSubscriber extends AbstractActor{

    private static Logger LOGGER = LoggerFactory.getLogger(BusAckSubscriber.class);

    public static String init = "init";
    public static String onComplete = "done";
    public static String ack = "ack";
    public static String workDone = "workDone";

    public final ByClassLookupEventBus eventBus;

    public BusAckSubscriber(ByClassLookupEventBus eventBus) {
        this.eventBus = eventBus;
    }

    private ActorRef streamSender;
    @Override
    public void preStart() throws Exception {
        super.preStart();
        LOGGER.info("Subscribing {} to messages of type {}", getSelf(), BusSubscriber.SubscriberDone.class);
        eventBus.subscribe(getSelf(), BusSubscriber.SubscriberDone.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals(init, x -> {
                    LOGGER.info("BusAckSubsriberSink Initialized, Sending Ack to {}", getSender());
                    getSender().tell(ack, getSelf());
                    streamSender = getSender();
                })
                .match(BusDispatcher.Message.class, message -> {
                    LOGGER.info("Received {} from {} at the end of the Stream. Waiting for Ack from the EventBus", message, getSender());
                })
                .match(BusSubscriber.SubscriberDone.class, message -> {
                    LOGGER.info("Received Message {} in {}", message, getSelf());
                    LOGGER.info("Sending message BackPressure Ack for {} to {}", message.processedMessage, streamSender);
                    streamSender.tell(ack, getSelf());
                })
                .matchEquals(onComplete, x -> {
                    getSender().tell(ack, getSelf());
                    LOGGER.info("BusAckSubsriberSink done!");
                })
                .build();
    }

    @Override
    public void postStop() throws Exception {
        eventBus.unsubscribe(getSelf());
        LOGGER.info("Unsubscribing {} to messages of type {}", getSelf(), BusDispatcher.Message.class);
        super.postStop();
    }

    static public class SubscriberDone {
        public final BusDispatcher.Message  processedMessage;

        public SubscriberDone(BusDispatcher.Message processedMessage) {
            this.processedMessage = processedMessage;
        }
    }

    public static Props props(ByClassLookupEventBus eventBus) {
        return Props.create(BusAckSubscriber.class, () -> new BusAckSubscriber(eventBus));
    }

}
