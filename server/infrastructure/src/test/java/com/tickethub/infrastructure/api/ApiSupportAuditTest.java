package com.tickethub.infrastructure.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;
import com.tickethub.infrastructure.audit.AuditEntry;
import com.tickethub.infrastructure.audit.AuditOutcome;
import com.tickethub.infrastructure.audit.AuditTrail;
import com.tickethub.infrastructure.web.CorrelationIdFilter;

class ApiSupportAuditTest {

    private final List<AuditEntry> entries = new ArrayList<>();

    private final UseCase<String, Either<Notification, String>> useCase = new UseCase<>() {
        @Override
        public Either<Notification, String> execute(final String input) {
            return Either.right("ok-" + input);
        }
    };

    @BeforeEach
    void setUp() {
        entries.clear();
        MDC.put(CorrelationIdFilter.CORRELATION_ID_KEY, "corr-1");
        MDC.put(CorrelationIdFilter.ACTOR_KEY, "alice");
        ApiSupport.configureResilience(ResiliencePolicy.disabled());
        ApiSupport.configureAuditTrail(entries::add);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        ApiSupport.configureResilience(ResiliencePolicy.disabled());
        ApiSupport.configureAuditTrail(AuditTrail.noop());
    }

    @Test
    void givenSuccess_whenExecute_thenRecordsSuccessEntry() {
        final var output = ApiSupport.execute(useCase, "in");

        assertEquals("ok-in", output);
        assertEquals(1, entries.size());
        final var entry = entries.get(0);
        assertEquals(AuditOutcome.SUCCESS, entry.outcome());
        assertEquals("corr-1", entry.correlationId());
        assertEquals("alice", entry.actor());
        assertNotNull(entry.occurredAt());
        assertTrue(entry.durationMs() >= 0);
    }

    @Test
    void givenNotFound_whenExecute_thenRecordsNotFoundEntry() {
        final UseCase<String, Either<Notification, String>> missing = new UseCase<>() {
            @Override
            public Either<Notification, String> execute(final String input) {
                return Either.left(Notification.create(new Error("Spot not found: 1")));
            }
        };

        assertThrows(ApiValidationException.class, () -> ApiSupport.execute(missing, "in"));

        assertEquals(1, entries.size());
        assertEquals(AuditOutcome.NOT_FOUND, entries.get(0).outcome());
        assertEquals("Spot not found: 1", entries.get(0).error());
    }

    @Test
    void givenValidationError_whenExecute_thenRecordsValidationEntry() {
        final UseCase<String, Either<Notification, String>> invalid = new UseCase<>() {
            @Override
            public Either<Notification, String> execute(final String input) {
                return Either.left(Notification.create(new Error("bad")));
            }
        };

        assertThrows(ApiValidationException.class, () -> ApiSupport.execute(invalid, "in"));

        assertEquals(AuditOutcome.VALIDATION_ERROR, entries.get(0).outcome());
    }

    @Test
    void givenInfraFailure_whenExecute_thenRecordsInfraErrorEntry() {
        final UseCase<String, Either<Notification, String>> broken = new UseCase<>() {
            @Override
            public Either<Notification, String> execute(final String input) {
                return Either.left(Notification.create(new IllegalStateException("db down")));
            }
        };

        assertThrows(IllegalStateException.class, () -> ApiSupport.execute(broken, "in"));

        assertEquals(AuditOutcome.INFRA_ERROR, entries.get(0).outcome());
    }

    @Test
    void givenUnavailable_whenExecute_thenRecordsUnavailableEntry() {
        final UseCase<String, Either<Notification, String>> down = new UseCase<>() {
            @Override
            public Either<Notification, String> execute(final String input) {
                return Either.left(Notification.create(
                        new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "nope")));
            }
        };

        final var exception = assertThrows(ResponseStatusException.class,
                () -> ApiSupport.execute(down, "in"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
        assertEquals(AuditOutcome.UNAVAILABLE, entries.get(0).outcome());
    }

    @Test
    void givenFailingTrail_whenExecute_thenRequestStillSucceeds() {
        ApiSupport.configureAuditTrail(entry -> {
            throw new IllegalStateException("trail down");
        });

        assertEquals("ok-in", ApiSupport.execute(useCase, "in"));
    }
}
