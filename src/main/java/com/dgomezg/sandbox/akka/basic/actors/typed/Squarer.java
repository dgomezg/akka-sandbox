package com.dgomezg.sandbox.akka.basic.actors.typed;


import akka.japi.Option;
import scala.concurrent.Future;

public interface Squarer {

    void squareDontCare(int i); //fire and forget

    Future<Integer> square (int i); //non-blocking send-request-reply

    Option<Integer> squareNowPlease(int i); //blocking send-request-reply;

    int squareNow(int i); //blocking send-request-reply;

}
