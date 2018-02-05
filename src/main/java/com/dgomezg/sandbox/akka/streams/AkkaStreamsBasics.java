package com.dgomezg.sandbox.akka.streams;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;

public class AkkaStreamsBasics {

    public static void main(String[] args) {

        ActorSystem actorSystem = ActorSystem.create("StreamBasics");
        Materializer materializer = ActorMaterializer.create(actorSystem);

        Source<Integer, NotUsed> source =
                Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

        Sink<Integer, CompletionStage<Integer>> sink =
                Sink.<Integer, Integer>fold(0, (agrregated, next) -> agrregated + next);

        RunnableGraph<CompletionStage<Integer>> stream = source.toMat(sink, Keep.right());

        CompletionStage<Integer> done = stream.run(materializer);
        done.whenComplete((i, t) -> System.out.println("I = " + i));
        done.thenRun(actorSystem::terminate);


    }
}
