package com.tickethub.domain;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.tickethub.domain.AggregateRoot;
import com.tickethub.domain.event.DomainEvent;

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
    void startsWithNoEvents() {
        assertTrue(new DummyAggregate().domainEvents().isEmpty());
    }

    @Test
    void registersEvents() {
        final var aggregate = new DummyAggregate();
        final var event = new DummyEvent();

        aggregate.registerEvent(event);

        assertEquals(1, aggregate.domainEvents().size());
        assertSame(event, aggregate.domainEvents().get(0));
    }

    @Test
    void ignoresNullEvents() {
        final var aggregate = new DummyAggregate();

        aggregate.registerEvent(null);

        assertTrue(aggregate.domainEvents().isEmpty());
    }

    @Test
    void domainEventsListIsUnmodifiable() {
        final var aggregate = new DummyAggregate();


        final var exception = assertThrows(UnsupportedOperationException.class,
                () -> aggregate.domainEvents().add(new DummyEvent()));

        assertNotNull(exception);

    }

    @Test
    void clearDomainEventsEmptiesTheList() {
        final var aggregate = new DummyAggregate();
        aggregate.registerEvent(new DummyEvent());

        aggregate.clearDomainEvents();

        assertTrue(aggregate.domainEvents().isEmpty());
    }
}
