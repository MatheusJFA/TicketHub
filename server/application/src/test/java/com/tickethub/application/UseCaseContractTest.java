package com.tickethub.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UseCaseContractTest {

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

    @Test
    void stubCanReturnNullWithoutChangingTheGenericContract() {
        final UseCase<String, String> useCase = new UseCase<>() {
            @Override
            public String execute(final String input) {
                return null;
            }
        };

        assertNull(useCase.execute("input"));
    }
}
