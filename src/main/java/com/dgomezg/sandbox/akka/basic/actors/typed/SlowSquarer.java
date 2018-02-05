package com.dgomezg.sandbox.akka.basic.actors.typed;

import akka.dispatch.Futures;
import akka.japi.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;

public class SlowSquarer implements Squarer {

    private static Logger LOGGER = LoggerFactory.getLogger(SlowSquarer.class);

    private final String name;

    public SlowSquarer() {
        this("default");
    }

    public SlowSquarer(String name) {
        this.name = name;
    }

    @Override
    public void squareDontCare(int i) {
        LOGGER.info("Squaring {} even though nobody cares", i);
        takeSomeTime();
        int result = i * i; //Not returned because nobody wants the result;
    }

    @Override
    public Future<Integer> square(int i) {
        LOGGER.info("Squaring {} returning a future " , i);
        takeSomeTime();
        return Futures.successful(i*i);
    }

    @Override
    public Option<Integer> squareNowPlease(int i) {
        LOGGER.info("Squaring {} returning an option that will block for the result" , i);
        takeSomeTime();
        return Option.some(i*i);
    }

    @Override
    public int squareNow(int i) {
        LOGGER.info("Squaring now {} returning the value" , i);
        takeSomeTime();
        return i*i;
    }

    private void takeSomeTime() {
        try {
            Thread.sleep(1_000);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

}
