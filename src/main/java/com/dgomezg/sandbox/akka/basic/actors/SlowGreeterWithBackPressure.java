package com.dgomezg.sandbox.akka.basic.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class SlowGreeterWithBackPressure extends AbstractActor {

    private static Logger LOGGER = LoggerFactory.getLogger(SlowGreeterWithBackPressure.class);

    public static Props props(String message, ActorRef printerActor) {
        return Props.create(SlowGreeterWithBackPressure.class, () -> new SlowGreeterWithBackPressure(message, printerActor));
    }

    public static String init = "init";
    public static String onComplete = "done";
    public static String ack = "ack";
    public static String workDone = "workDone";

    static public class Greet {
        public final String message;

        public Greet() {
            this("greetings");
        }

        public Greet(String message) {
            this.message = message;
        }
    }

    static public class Init {
        public Init() {}
    }

    static public class Done {
        public Done() {}
    }

    static public class Ack {
        public Ack() {}
    }

    private final String message;
    private final ActorRef printerActor;

    public SlowGreeterWithBackPressure(String message, ActorRef printerActor) {
        this.message = message;
        this.printerActor = printerActor;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals(init, x -> {
                    LOGGER.info("SlowGreeterWithBackPressure Initialized, Sending Ack to {}", getSender());
                    getSender().tell(ack, getSelf());
                })
                .match(Greet.class, x ->  {
                    LOGGER.info("SlowGreeterWithBackpressure greeting with {}", message);
                    printerActor.tell(new Printer.Greeting(message), getSelf());
                    getSender().tell(ack, getSelf());
                })
                .matchEquals(onComplete, x -> {
                    getSender().tell(ack, getSelf());
                    LOGGER.info("SlowGreeterWithBackPressure done!");
                })
                .build();
    }

}
