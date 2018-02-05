package com.dgomezg.sandbox.akka.basic.bus;

import akka.actor.AbstractActor;
import akka.actor.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageProcessorWithBusAck extends AbstractActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessorWithBusAck.class);

    private final ByClassLookupEventBus ackEventBus;

    public MessageProcessorWithBusAck(ByClassLookupEventBus ackEventBus) {
        this.ackEventBus = ackEventBus;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        LOGGER.info("Starting MessageProcessor {}", getSelf());

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BusDispatcher.Message.class, message -> {
                    LOGGER.info("Received Message {} in {}", message, getSelf());
                    long sleepTime = Math.round(Math.random()*50_000);
                    LOGGER.info("Sleeping for {} ms after processing {} ", sleepTime, message);
                    Thread.sleep(sleepTime);
                    LOGGER.info("Sending message completion for {}", message);
                    ackEventBus.publish(new BusSubscriber.SubscriberDone(message));
                })
                .build();
    }

    public static Props props(ByClassLookupEventBus ackEventBus) {
        return Props.create(MessageProcessorWithBusAck.class,ackEventBus);
    }

}
