package com.dgomezg.sandbox.akka.basic.bus;

import akka.actor.ActorRef;
import akka.event.japi.LookupEventBus;

/**
 * Publishes the payload of the MsgEnvelope when the Class of the
 * MsgEnvelope equals the Class specified when subscribing.
 */
public class ByClassLookupEventBus extends LookupEventBus<Object, ActorRef, Class> {

    // Used for extracting the classifier from the incoming events
    @Override
    public Class classify(Object event) {
        return event.getClass();
    }

    // will be invoked for each event for all subscribers which registered themselves
    // for the event’s classifier
    @Override
    public void publish(Object event, ActorRef subscriber) {
        subscriber.tell(event, ActorRef.noSender());
    }

    @Override
    public int mapSize() {
        return 128;
    }

    @Override
    public int compareSubscribers(ActorRef a, ActorRef b) {
        return a.compareTo(b);
    }


}
