package com.dgomezg.sandbox.akka.basic;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.dgomezg.sandbox.akka.basic.actors.Greeter;
import com.dgomezg.sandbox.akka.basic.actors.Printer;

import java.io.IOException;

import static com.dgomezg.sandbox.akka.basic.actors.Printer.*;
import static com.dgomezg.sandbox.akka.basic.actors.Greeter.*;

public class SimpleAkkaActor {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("actor");
        try {
            //#create-actors
            final ActorRef printerActor =
                    system.actorOf(Printer.props()/*, "printerActor"*/);
            final ActorRef howdyGreeter =
                    system.actorOf(Greeter.props("Howdy", printerActor), "howdyGreeter");
            final ActorRef helloGreeter =
                    system.actorOf(Greeter.props("Hello", printerActor), "helloGreeter");
            final ActorRef goodDayGreeter =
                    system.actorOf(Greeter.props("Good day", printerActor), "goodDayGreeter");

            final ActorRef goodDayGreeter2 =
                    system.actorFor("akka://actor/user/goodDayGreeter");
            //#create-actors

            //#main-send-messages
            howdyGreeter.tell(new WhoToGreet("Akka"), ActorRef.noSender());
            howdyGreeter.tell(new Greet(), ActorRef.noSender());

            howdyGreeter.tell(new WhoToGreet("Lightbend"), ActorRef.noSender());
            howdyGreeter.tell(new Greet(), ActorRef.noSender());

            helloGreeter.tell(new WhoToGreet("Java"), ActorRef.noSender());
            helloGreeter.tell(new Greet(), ActorRef.noSender());

            goodDayGreeter.tell(new WhoToGreet("Play"), ActorRef.noSender());
            goodDayGreeter.tell(new Greet(), ActorRef.noSender());
            //#main-send-messages

            goodDayGreeter2.tell(new WhoToGreet("Play"), ActorRef.noSender());
            goodDayGreeter2.tell(new Greet(), ActorRef.noSender());

            System.out.println("Name for Hello Greeter -> " + helloGreeter.path().name());
            System.out.println("Path for Hello Greeter -> " + helloGreeter.path());

            System.out.println("Name for Printer -> " + printerActor.path().name());
            System.out.println("Path for Printer -> " + printerActor.path());

            System.out.println(">>> Press ENTER to exit <<<");
            System.in.read();
        } catch (IOException ioe) {
        } finally {
            system.terminate();
        }


    }
}
