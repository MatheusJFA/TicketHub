package com.tickethub.domain.core.payment;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.UUID;

@DisplayName("Charge ID")
class ChargeIDTest {

    @Test
    @DisplayName("Given no value, when generate, then return valid uuid")
    void givenNoValue_whenGenerate_thenReturnValidUUID() {
        ChargeID id = ChargeID.generate();

        assertEquals(id.getValue(), UUID.fromString(id.getValue()).toString());
    }

    @Test
    @DisplayName("Given two generated ids, when compare, then not be equal")
    void givenTwoGeneratedIDs_whenCompare_thenNotBeEqual() {
        assertNotEquals(ChargeID.generate(), ChargeID.generate());
    }

    @Test
    @DisplayName("Given a value, when from, then preserve value")
    void givenAValue_whenFrom_thenPreserveValue() {
        String value = "ch_1234567890ab";

        assertEquals(value, ChargeID.from(value).getValue());
    }

    @Test
    @DisplayName("Given a null value, when from, then throw null pointer exception")
    void givenANullValue_whenFrom_thenThrowNullPointerException() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> ChargeID.from(null));

        assertEquals("'ChargeID' should not be null", exception.getMessage());
    }
}
