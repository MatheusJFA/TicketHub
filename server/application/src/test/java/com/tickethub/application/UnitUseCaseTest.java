package com.tickethub.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit use case")
class UnitUseCaseTest {

    static class StoreUseCase extends UnitUseCase<String> {
        final AtomicReference<String> stored = new AtomicReference<>();

        @Override
        public Optional<Notification> execute(final String input) {
            stored.set(input);
            return Optional.empty();
        }
    }

    static class FailingUseCase extends UnitUseCase<String> {
        @Override
        public Optional<Notification> execute(final String input) {
            return Optional.of(Notification.create(new Error("boom: " + input)));
        }
    }

    @Test
    @DisplayName("Executes without output")
    void executesWithoutOutput() {
        final var useCase = new StoreUseCase();

        final var result = useCase.execute("value");

        assertTrue(result.isEmpty());
        assertEquals("value", useCase.stored.get());
    }

    @Test
    @DisplayName("Stub receives null input")
    void stubReceivesNullInput() {
        final var useCase = new StoreUseCase();

        useCase.execute(null);

        assertNull(useCase.stored.get());
    }

    @Test
    @DisplayName("Reports failure as notification")
    void reportsFailureAsNotification() {
        final var result = new FailingUseCase().execute("value");

        assertTrue(result.isPresent());
        assertEquals("boom: value", result.orElseThrow().firstError().message());
    }
}
