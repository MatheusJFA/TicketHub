package com.tickethub.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tickethub.domain.validation.ValidationHandler;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Entity audit")
class EntityAuditTest {

    @Test
    @DisplayName("Given an entity, when created, then initialize audit dates")
    void givenAnEntity_whenCreated_thenInitializeAuditDates() {
        final var entity = new TestEntity(TestEntityId.generate());

        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
        assertNull(entity.getDeletedAt());
        assertFalse(entity.isDeleted());
    }

    @Test
    @DisplayName("Given an active entity, when deleted, then set deleted at and update timestamp")
    void givenAnActiveEntity_whenDeleted_thenSetDeletedAtAndUpdateTimestamp() throws InterruptedException {
        final var entity = new TestEntity(TestEntityId.generate());
        final var previousUpdate = entity.getUpdatedAt();

        Thread.sleep(1);
        entity.delete();

        assertTrue(entity.isDeleted());
        assertNotNull(entity.getDeletedAt());
        assertTrue(entity.getUpdatedAt().isAfter(previousUpdate));
    }

    @Test
    @DisplayName("Given a deleted entity, when restored, then clear deleted at and update timestamp")
    void givenADeletedEntity_whenRestored_thenClearDeletedAtAndUpdateTimestamp() throws InterruptedException {
        final var entity = new TestEntity(TestEntityId.generate());
        entity.delete();
        final var previousUpdate = entity.getUpdatedAt();

        Thread.sleep(1);
        entity.restore();

        assertFalse(entity.isDeleted());
        assertNull(entity.getDeletedAt());
        assertTrue(entity.getUpdatedAt().isAfter(previousUpdate));
    }

    private static final class TestEntity extends Entity<TestEntityId> {
        private TestEntity(TestEntityId id) {
            super(id);
        }

        @Override
        public void validate(final ValidationHandler handler) {}
    }

    private static final class TestEntityId extends Identifier {
        private final UUID value;

        private TestEntityId(UUID value) {
            this.value = value;
        }

        private static TestEntityId generate() {
            return new TestEntityId(UUID.randomUUID());
        }

        @Override
        public String getValue() {
            return value.toString();
        }
    }
}
