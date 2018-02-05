package com.dgomezg.sandbox.akka.basic.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Printer extends AbstractActor {

    static public Props props() {
        return Props.create(Printer.class, () -> new Printer());
    }

    //#printer-messages
    static public class Greeting {
        public final String message;

        public Greeting(String message) {
            this.message = message;
        }
    }
    //#printer-messages

    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public Printer() {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Greeting.class, greeting -> {
                    log.info("Looging message " + greeting.message);
                    long sleepTime = Math.round(Math.random() * 5_000);
                    log.info("Sleeping for {} ms" , sleepTime);
                    Thread.sleep(sleepTime);
                    //getSender().tell(SlowGreeterWithBackPressure.workDone, getSelf());
                })
                .build();
    }


}
