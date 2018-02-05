package com.dgomezg.sandbox.akka.basic;

import akka.actor.ActorSystem;
import akka.actor.TypedActor;
import akka.actor.TypedProps;
import akka.japi.Creator;
import akka.japi.Option;
import com.dgomezg.sandbox.akka.basic.actors.typed.SlowSquarer;
import com.dgomezg.sandbox.akka.basic.actors.typed.Squarer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class SimpleTypedActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTypedActor.class);

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception, Throwable {
        ActorSystem system = ActorSystem.create("actorSystem");

        LOGGER.info("Creating the TypedActor");
        Squarer squarerActor =
                TypedActor.get(system)
                .typedActorOf(new TypedProps<>(SlowSquarer::new));

        LOGGER.info("Squaring something I don't care");
        squarerActor.squareDontCare(10);
        LOGGER.info("Squaring something for the future");
        Future<Integer> squaredFuture = squarerActor.square(10);
        LOGGER.info("Squaring something optional");
        Option<Integer> squaredOption = squarerActor.squareNowPlease(10);
        LOGGER.info("Squaring something now");
        int squared = squarerActor.squareNow(10);
        LOGGER.info("Result of Squared is {}, {}, {}", squaredFuture.value().get().get(), squaredOption.get(), squared);


        //TypedActor.get(system).stop(squared); //synchronous
        TypedActor.get(system).poisonPill(squared);
        system.terminate();


    }
}
