package com.tickethub.domain.validation;

import static org.junit.jupiter.api.Assertions.*;

import com.tickethub.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Notification")
class NotificationTest {

    @Test
    @DisplayName("Starts empty")
    void startsEmpty() {
        final var notification = Notification.create();

        assertFalse(notification.hasError());
        assertTrue(notification.getErrors().isEmpty());
        assertNull(notification.firstError());
    }

    @Test
    @DisplayName("Create with error starts with that error")
    void createWithErrorStartsWithThatError() {
        final var error = new Error("boom");

        final var notification = Notification.create(error);

        assertTrue(notification.hasError());
        assertEquals(error, notification.firstError());
    }

    @Test
    @DisplayName("Create with throwable wraps its message")
    void createWithThrowableWrapsItsMessage() {
        final var notification = Notification.create(new RuntimeException("kaput"));

        assertEquals(new Error("kaput"), notification.firstError());
    }

    @Test
    @DisplayName("Append accumulates and returns this")
    void appendAccumulatesAndReturnsThis() {
        final var notification = Notification.create();

        final var returned = notification.append(new Error("a")).append(new Error("b"));

        assertSame(notification, returned);
        assertEquals(2, notification.getErrors().size());
    }

    @Test
    @DisplayName("Append handler merges its errors")
    void appendHandlerMergesItsErrors() {
        final var other = Notification.create(new Error("a")).append(new Error("b"));
        final var notification = Notification.create();

        notification.append(other);

        assertEquals(other.getErrors(), notification.getErrors());
    }

    @Test
    @DisplayName("Validate returns value on success")
    void validateReturnsValueOnSuccess() {
        final var notification = Notification.create();

        final var result = notification.validate(() -> "ok");

        assertEquals("ok", result);
        assertFalse(notification.hasError());
    }

    @Test
    @DisplayName("Validate captures domain exception as error")
    void validateCapturesDomainExceptionAsError() {
        final var notification = Notification.create();

        final var result = notification.validate(() -> {
            throw new DomainException("invalid");
        });

        assertNull(result);
        assertEquals(new Error("invalid"), notification.firstError());
    }

    @Test
    @DisplayName("Validate captures any throwable as error")
    void validateCapturesAnyThrowableAsError() {
        final var notification = Notification.create();

        final var result = notification.validate(() -> {
            throw new IllegalStateException("broken");
        });

        assertNull(result);
        assertEquals(new Error("broken"), notification.firstError());
    }
}
