package com.tickethub.domain.core.spot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Spot ID")
class SpotIDTest {

    @Test
    @DisplayName("Given no value, when generate, then return valid uuid")
    void givenNoValue_whenGenerate_thenReturnValidUUID() {
        SpotID id = SpotID.generate();

        assertEquals(id.getValue(), UUID.fromString(id.getValue()).toString());
    }

    @Test
    @DisplayName("Given two generated i ds, when compare, then not be equal")
    void givenTwoGeneratedIDs_whenCompare_thenNotBeEqual() {
        assertNotEquals(SpotID.generate(), SpotID.generate());
    }

    @Test
    @DisplayName("Given a value, when from, then preserve value")
    void givenAValue_whenFrom_thenPreserveValue() {
        String value = "cf4361d9-8e93-4c53-9ac5-629680d98469";

        assertEquals(value, SpotID.from(value).getValue());
    }

    @Test
    @DisplayName("Given a null value, when from, then throw null pointer exception")
    void givenANullValue_whenFrom_thenThrowNullPointerException() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> SpotID.from(null));

        assertEquals("'SpotID' should not be null", exception.getMessage());
    }
}
