package com.tickethub.application;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NullaryUseCaseTest {

    static class GreetingUseCase extends NullaryUseCase<String> {
        @Override
        public String execute() {
            return "hello";
        }
    }

    @Test
    void executesWithoutInput() {
        assertEquals("hello", new GreetingUseCase().execute());
    }

    @Test
    void stubCanProduceDifferentResultsOnEachExecution() {
        final var counter = new AtomicInteger();
        final NullaryUseCase<Integer> useCase = new NullaryUseCase<>() {
            @Override
            public Integer execute() {
                return counter.incrementAndGet();
            }
        };

        assertEquals(1, useCase.execute());
        assertEquals(2, useCase.execute());
    }
}
