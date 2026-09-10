package com.tickethub.application;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnitUseCaseTest {

    static class StoreUseCase extends UnitUseCase<String> {
        final AtomicReference<String> stored = new AtomicReference<>();

        @Override
        public void execute(final String input) {
            stored.set(input);
        }
    }

    @Test
    void executesWithoutOutput() {
        final var useCase = new StoreUseCase();

        useCase.execute("value");

        assertEquals("value", useCase.stored.get());
    }
}
