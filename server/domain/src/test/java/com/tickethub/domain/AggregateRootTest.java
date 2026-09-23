package com.tickethub.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tickethub.domain.event.DomainEvent;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Aggregate root")
class AggregateRootTest {

    record DummyEvent(Instant occurredOn) implements DomainEvent {
        DummyEvent() {
            this(Instant.now());
        }
    }

    static class DummyAggregate extends AggregateRoot<EntityTest.DummyId> {
        DummyAggregate() {
            super(new EntityTest.DummyId("1"));
        }
    }

    @Test
    @DisplayName("Starts with no events")
    void startsWithNoEvents() {
        assertTrue(new DummyAggregate().domainEvents().isEmpty());
    }

    @Test
    @DisplayName("Registers events")
    void registersEvents() {
        final var aggregate = new DummyAggregate();
        final var event = new DummyEvent();

        aggregate.registerEvent(event);

        assertEquals(1, aggregate.domainEvents().size());
        assertSame(event, aggregate.domainEvents().get(0));
    }

    @Test
    @DisplayName("Ignores null events")
    void ignoresNullEvents() {
        final var aggregate = new DummyAggregate();

        aggregate.registerEvent(null);

        assertTrue(aggregate.domainEvents().isEmpty());
    }

    @Test
    @DisplayName("Domain events list is unmodifiable")
    void domainEventsListIsUnmodifiable() {
        final var aggregate = new DummyAggregate();

        final var exception = assertThrows(
                UnsupportedOperationException.class,
                () -> aggregate.domainEvents().add(new DummyEvent()));

        assertNotNull(exception);
    }

    @Test
    @DisplayName("Clear domain events empties the list")
    void clearDomainEventsEmptiesTheList() {
        final var aggregate = new DummyAggregate();
        aggregate.registerEvent(new DummyEvent());

        aggregate.clearDomainEvents();

        assertTrue(aggregate.domainEvents().isEmpty());
    }
}
