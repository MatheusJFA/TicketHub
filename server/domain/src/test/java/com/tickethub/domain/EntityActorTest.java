package com.tickethub.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.tickethub.domain.validation.ValidationHandler;

class EntityActorTest {

    private static final class ActorEntity extends Entity<EntityTest.DummyId> {
        ActorEntity(final EntityTest.DummyId id) {
            super(id);
        }

        ActorEntity(final EntityTest.DummyId id, final String createdBy) {
            super(id, Instant.now(), Instant.now(), null, createdBy, createdBy);
        }

        ActorEntity(final EntityTest.DummyId id, final Instant createdAt, final Instant updatedAt,
                final Instant deletedAt, final String createdBy, final String lastModifiedBy) {
            super(id, createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
        }

        void touch(final String actor) {
            markAsUpdatedBy(actor);
        }

        @Override
        public void validate(final ValidationHandler handler) {
        }
    }

    @Test
    void givenNoActor_whenCreated_thenActorFieldsAreNull() {
        final var entity = new ActorEntity(new EntityTest.DummyId("1"));

        assertNull(entity.getCreatedBy());
        assertNull(entity.getLastModifiedBy());
    }

    @Test
    void givenActors_whenCreated_thenActorsAreStored() {
        final var now = Instant.now();
        final var entity = new ActorEntity(new EntityTest.DummyId("1"), now, now, null, "alice", "bob");

        assertEquals("alice", entity.getCreatedBy());
        assertEquals("bob", entity.getLastModifiedBy());
    }

    @Test
    void givenAnEntity_whenTouchedByActor_thenUpdatesTimestampAndActor() throws InterruptedException {
        final var entity = new ActorEntity(new EntityTest.DummyId("1"), "alice");
        final var previousUpdate = entity.getUpdatedAt();

        Thread.sleep(1);
        entity.touch("bob");

        assertTrue(entity.getUpdatedAt().isAfter(previousUpdate));
        assertEquals("bob", entity.getLastModifiedBy());
        assertEquals("alice", entity.getCreatedBy());
    }
}
