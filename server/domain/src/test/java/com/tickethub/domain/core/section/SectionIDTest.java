package com.tickethub.domain.core.section;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class SectionIDTest {

    @Test
    void givenNoValue_whenGenerate_thenReturnValidUUID() {
        SectionID id = SectionID.generate();

        assertEquals(id.getValue(), UUID.fromString(id.getValue()).toString());
    }

    @Test
    void givenTwoGeneratedIDs_whenCompare_thenNotBeEqual() {
        assertNotEquals(SectionID.generate(), SectionID.generate());
    }

    @Test
    void givenAValue_whenFrom_thenPreserveValue() {
        String value = "cf4361d9-8e93-4c53-9ac5-629680d98469";

        assertEquals(value, SectionID.from(value).getValue());
    }

    @Test
    void givenANullValue_whenFrom_thenThrowNullPointerException() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> SectionID.from(null));

        assertEquals("'SectionID' should not be null", exception.getMessage());
    }
}
