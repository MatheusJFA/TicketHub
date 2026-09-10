package com.tickethub.application;

import org.junit.jupiter.api.Test;

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
}
