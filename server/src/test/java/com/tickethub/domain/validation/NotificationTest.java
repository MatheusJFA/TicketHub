package com.tickethub.domain.validation;

import com.tickethub.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    void startsEmpty() {
        final var notification = Notification.create();

        assertFalse(notification.hasError());
        assertTrue(notification.getErrors().isEmpty());
        assertNull(notification.firstError());
    }

    @Test
    void createWithErrorStartsWithThatError() {
        final var error = new Error("boom");

        final var notification = Notification.create(error);

        assertTrue(notification.hasError());
        assertEquals(error, notification.firstError());
    }

    @Test
    void createWithThrowableWrapsItsMessage() {
        final var notification = Notification.create(new RuntimeException("kaput"));

        assertEquals(new Error("kaput"), notification.firstError());
    }

    @Test
    void appendAccumulatesAndReturnsThis() {
        final var notification = Notification.create();

        final var returned = notification.append(new Error("a")).append(new Error("b"));

        assertSame(notification, returned);
        assertEquals(2, notification.getErrors().size());
    }

    @Test
    void appendHandlerMergesItsErrors() {
        final var other = Notification.create(new Error("a")).append(new Error("b"));
        final var notification = Notification.create();

        notification.append(other);

        assertEquals(other.getErrors(), notification.getErrors());
    }

    @Test
    void validateReturnsValueOnSuccess() {
        final var notification = Notification.create();

        final var result = notification.validate(() -> "ok");

        assertEquals("ok", result);
        assertFalse(notification.hasError());
    }

    @Test
    void validateCapturesDomainExceptionAsError() {
        final var notification = Notification.create();

        final var result = notification.validate(() -> {
            throw new DomainException("invalid");
        });

        assertNull(result);
        assertEquals(new Error("invalid"), notification.firstError());
    }

    @Test
    void validateCapturesAnyThrowableAsError() {
        final var notification = Notification.create();

        final var result = notification.validate(() -> {
            throw new IllegalStateException("broken");
        });

        assertNull(result);
        assertEquals(new Error("broken"), notification.firstError());
    }
}
