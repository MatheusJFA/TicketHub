package com.tickethub.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UseCaseTest {

    static class UppercaseUseCase extends UseCase<String, String> {
        @Override
        public String execute(final String input) {
            return input.toUpperCase();
        }
    }

    @Test
    void executesWithTypedInputAndOutput() {
        assertEquals("HI", new UppercaseUseCase().execute("hi"));
    }
}
