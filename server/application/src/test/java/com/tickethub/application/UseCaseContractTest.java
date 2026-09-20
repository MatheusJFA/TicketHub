package com.tickethub.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Use case contract")
class UseCaseContractTest {

    static class UppercaseUseCase extends UseCase<String, String> {
        @Override
        public String execute(final String input) {
            return input.toUpperCase();
        }
    }

    @Test
    @DisplayName("Executes with typed input and output")
    void executesWithTypedInputAndOutput() {
        assertEquals("HI", new UppercaseUseCase().execute("hi"));
    }

    @Test
    @DisplayName("Stub can return null without changing the generic contract")
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
