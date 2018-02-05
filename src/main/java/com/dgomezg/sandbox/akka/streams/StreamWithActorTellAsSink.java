package com.dgomezg.sandbox.akka.streams;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.*;
import com.dgomezg.sandbox.akka.basic.actors.Greeter;
import com.dgomezg.sandbox.akka.basic.actors.Printer;
import com.dgomezg.sandbox.akka.basic.actors.SlowGreeterWithBackPressure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamWithActorTellAsSink {

    public static Logger LOGGER = LoggerFactory.getLogger(StreamWithActorTellAsSink.class);

    public static void main(String[] args) {

        ActorSystem actorSystem = ActorSystem.create("StreamWithActorTellAsSink");
        Materializer materializer = ActorMaterializer.create(actorSystem);

        ActorRef printerActor = actorSystem.actorOf(Printer.props(), "printerActor");
        ActorRef slowGreeterActor = actorSystem.actorOf(SlowGreeterWithBackPressure.props("SlowGreeterWithBackPressure", printerActor), "slowGreeterActor");


        final Source<Integer, NotUsed> source = Source.range(1, 1_000);

        final Flow<Integer, SlowGreeterWithBackPressure.Greet, NotUsed> flow =
                Flow.of(Integer.class)
                    .flatMapMerge(4, number -> {
                        LOGGER.info("Generating new message " + number + " for the stream");
                        return Source.single(new SlowGreeterWithBackPressure.Greet(" Hi " + number));
                    } );

        final Sink<SlowGreeterWithBackPressure.Greet, NotUsed> actorSink = tellActorSinkWithAck(slowGreeterActor);


        final RunnableGraph<NotUsed> streamWithBackPressure = source.toMat(flow.toMat(actorSink, Keep.right()), Keep.right());

        long start = System.currentTimeMillis();
        streamWithBackPressure.run(materializer);
        LOGGER.info("Stream with backpressure done in {} ms.", System.currentTimeMillis() - start);

    }

    public static Sink<SlowGreeterWithBackPressure.Greet, NotUsed> tellActorSink(ActorRef actor) {
        return Sink.actorRef(actor, NotUsed.getInstance());
    }

    public static Sink<SlowGreeterWithBackPressure.Greet, NotUsed> tellActorSinkWithAck(ActorRef actor) {
        return Sink.actorRefWithAck(actor,
                    SlowGreeterWithBackPressure.init,
                    SlowGreeterWithBackPressure.ack,
                    SlowGreeterWithBackPressure.onComplete,
                    Throwable::getMessage);
    }


}
