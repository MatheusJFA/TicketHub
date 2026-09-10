package com.tickethub.domain;

import com.tickethub.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    static class DummyId extends Identifier {
        private final String value;

        DummyId(final String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }

        @Override
        public boolean equals(final Object o) {
            return o instanceof DummyId other && value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
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
        assertThrows(DomainException.class, () -> new DummyEntity(null));
    }

    @Test
    void getIdReturnsTheId() {
        final var id = new DummyId("42");

        assertEquals(id, new DummyEntity(id).getId());
    }
}
