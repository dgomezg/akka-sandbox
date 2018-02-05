package com.dgomezg.sandbox.akka.basic.bus;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.RoundRobinPool;
import com.dgomezg.sandbox.akka.basic.actors.SlowGreeterWithBackPressure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class BusSubscriber extends AbstractActor{

    private static Logger LOGGER = LoggerFactory.getLogger(BusSubscriber.class);

    private final ByClassLookupEventBus eventBus;
    private ActorRef messageHandler;

    public BusSubscriber(ByClassLookupEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        messageHandler = getContext().actorOf(new RoundRobinPool(5).props(MessageProcessorWithBusAck.props(eventBus)));
        LOGGER.info("Subscribing {} to messages of type {}", getSelf(), BusDispatcher.Message.class);
        eventBus.subscribe(getSelf(), BusDispatcher.Message.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BusDispatcher.Message.class, message -> {
                    LOGGER.info("Received Message {} in {}", message, getSelf());
                    messageHandler.tell(message, getSender());
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
        return Props.create(BusSubscriber.class, () -> new BusSubscriber(eventBus));
    }

}
