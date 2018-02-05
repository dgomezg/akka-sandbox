package com.dgomezg.sandbox.akka.streams;

import akka.actor.ActorSystem;
import akka.stream.*;
import akka.stream.javadsl.*;

import akka.NotUsed;
import akka.util.ByteString;
import scala.concurrent.duration.Duration;

import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class AkkaStreamsQuickStart {

    public static void main(String[] args) {
        ActorSystem actorSystem = ActorSystem.create("QuickStart");
        Materializer materializer = ActorMaterializer.create(actorSystem);


        final Source<Integer, NotUsed> source = Source.range(1,100);

        //final CompletionStage<Done> done = source.runForeach(System.out::println, materializer);

        final Source<BigInteger, NotUsed> factorials =
                source.scan(BigInteger.ONE, (acc, next) -> acc.multiply(BigInteger.valueOf(next)));
        /*
        final CompletionStage<IOResult> done =
                factorials.map(num -> ByteString.fromString(num.toString() + "\n"))
                .runWith(FileIO.toPath(Paths.get("factorials.txt")), materializer);
                */
        /*
        final CompletionStage<IOResult> done =
                factorials.map(BigInteger::toString)
                        .runWith(lineSink("factorials2.txt"), materializer);
                        */
        final CompletionStage done =
                factorials.zipWith(Source.range(0,100), (num, idx) -> String.format("%3d! = %d", idx, num))
                        .throttle(1, Duration.create(100, TimeUnit.MILLISECONDS), 10, ThrottleMode.shaping())
                        .runForeach(System.out::println, materializer);


        done.thenRun(actorSystem::terminate);

    }

    public static Sink<String, CompletionStage<IOResult>> lineSink(String filename) {
        return Flow.of(String.class)
                .map(s -> ByteString.fromString(s.toString() + "\n"))
                .toMat(FileIO.toPath(Paths.get(filename)), Keep.right());
    }
}
