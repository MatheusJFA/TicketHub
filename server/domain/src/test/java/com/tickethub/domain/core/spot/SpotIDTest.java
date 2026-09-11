package com.tickethub.domain.core.spot;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class SpotIDTest {

    @Test
    void givenNoValue_whenGenerate_thenReturnValidUUID() {
        SpotID id = SpotID.generate();

        assertEquals(id.getValue(), UUID.fromString(id.getValue()).toString());
    }

    @Test
    void givenTwoGeneratedIDs_whenCompare_thenNotBeEqual() {
        assertNotEquals(SpotID.generate(), SpotID.generate());
    }

    @Test
    void givenAValue_whenFrom_thenPreserveValue() {
        String value = "cf4361d9-8e93-4c53-9ac5-629680d98469";

        assertEquals(value, SpotID.from(value).getValue());
    }

    @Test
    void givenANullValue_whenFrom_thenThrowNullPointerException() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> SpotID.from(null));

        assertEquals("'SpotID' should not be null", exception.getMessage());
    }
}
