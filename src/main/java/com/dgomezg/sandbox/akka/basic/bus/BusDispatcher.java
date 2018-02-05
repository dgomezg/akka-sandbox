package com.dgomezg.sandbox.akka.basic.bus;

import akka.actor.AbstractActor;
import akka.actor.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusDispatcher extends AbstractActor {

    private static Logger LOGGER = LoggerFactory.getLogger(BusDispatcher.class);

    static public class Message {
        public final Object payload;

        public Message(Object payload) {
            this.payload = payload;
        }

        @Override
        public String toString() {
            return payload.toString();
        }
    }

    private final ByClassLookupEventBus eventBus;

    public BusDispatcher(ByClassLookupEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Message.class, message -> {
                    LOGGER.info("Distpatching {} to bus", message);
                    eventBus.publish(message);
                })
                .build();
    }

    public static Props props(ByClassLookupEventBus eventBus) {
        return Props.create(BusDispatcher.class, () -> new BusDispatcher(eventBus));
    }

}
