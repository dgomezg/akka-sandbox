package com.dgomezg.sandbox.akka.streams;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Attributes;
import akka.stream.Materializer;
import akka.stream.javadsl.*;
import com.dgomezg.sandbox.akka.basic.actors.SlowGreeterWithBackPressure;
import com.dgomezg.sandbox.akka.basic.bus.BusAckSubscriber;
import com.dgomezg.sandbox.akka.basic.bus.BusDispatcher;
import com.dgomezg.sandbox.akka.basic.bus.BusSubscriber;
import com.dgomezg.sandbox.akka.basic.bus.ByClassLookupEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamWithBackPressureFromBus {

    private static Logger LOGGER = LoggerFactory.getLogger(StreamWithBackPressureFromBus.class);

    public static void main(String[] args) {
        ActorSystem actorSystem = ActorSystem.create("StreamWithBackPressureFromBus");
        ByClassLookupEventBus eventBus = new ByClassLookupEventBus();

        ActorRef busDispatcher = actorSystem.actorOf(BusDispatcher.props(eventBus), "busDispatcher");
        ActorRef ackBackpressure = actorSystem.actorOf(BusAckSubscriber.props(eventBus), "backpressureSink");
        actorSystem.actorOf(BusSubscriber.props(eventBus), "busSubscriber");

        Materializer materializer = ActorMaterializer.create(actorSystem);

        final Source<Integer, NotUsed> source = Source.range(1, 1_000);

      //  final Flow<Integer, BusDispatcher.Message, NotUsed> flow = getFlowWithFlapMapMerge(busDispatcher);

        final Flow<Integer, BusDispatcher.Message, NotUsed> flow = getFlatFlow(busDispatcher);

        RunnableGraph<NotUsed> stream = source.toMat(flow.toMat(tellActorSinkWithAck(ackBackpressure), Keep.left()), Keep.right());

        stream.run(materializer);
    }

    public static Sink<BusDispatcher.Message, NotUsed> tellActorSinkWithAck(ActorRef actor) {
        Sink sink = Sink.actorRefWithAck(actor,
                SlowGreeterWithBackPressure.init,
                SlowGreeterWithBackPressure.ack,
                SlowGreeterWithBackPressure.onComplete,
                Throwable::getMessage);
       //  sink = sink.addAttributes(Attributes.inputBuffer(4, 4));
        return sink;
    }

    public static Flow getFlowWithFlapMapMerge(ActorRef busDispatcher) {
        return Flow.of(Integer.class)
                .flatMapMerge(4, number -> {
                    LOGGER.info("Generating new message " + number + " for the stream");
                    return Source.single(new BusDispatcher.Message(" Hi " + number))
                            .map(message -> {
                                busDispatcher.tell(message, ActorRef.noSender());
                                return message;
                            });
                } );

    }

    public static Flow getFlatFlow(ActorRef busDispatcher) {
        return Flow.of(Integer.class)
                .map(number -> {
                    LOGGER.info("Generating new message " + number + " for the stream");
                    return new BusDispatcher.Message(" Hi " + number);
                })
                .map(message -> {
                    busDispatcher.tell(message, ActorRef.noSender());
                    return message;
                });
    }

}
