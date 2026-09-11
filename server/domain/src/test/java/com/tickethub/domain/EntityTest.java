package com.tickethub.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.tickethub.domain.Entity;
import com.tickethub.domain.Identifier;
import com.tickethub.domain.exception.DomainException;

class EntityTest {

    static class DummyId extends Identifier {
        private final String value;

        DummyId(final String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }


    }

    static class DummyEntity extends Entity<DummyId> {
        DummyEntity(final DummyId id) {
            super(id);
        }
    }

    @Test
    void entitiesWithSameIdAreEqual() {
        final var a = new DummyEntity(new DummyId("1"));
        final var b = new DummyEntity(new DummyId("1"));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void entitiesWithDifferentIdsAreNotEqual() {
        final var a = new DummyEntity(new DummyId("1"));
        final var b = new DummyEntity(new DummyId("2"));

        assertNotEquals(a, b);
    }

    @Test
    void nullIdThrowsDomainException() {
        final var exception = assertThrows(DomainException.class, () -> new DummyEntity(null));

        assertNotNull(exception);
        assertEquals("'id' should not be null", exception.getMessage());
    }

    @Test
    void getIdReturnsTheId() {
        final var id = new DummyId("42");

        assertEquals(id, new DummyEntity(id).getId());
    }
}
