package com.tickethub.domain.core.partner;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class PartnerIDTest {

    @Test
    void givenNoValue_whenGenerate_thenReturnValidUUID() {
        PartnerID id = PartnerID.generate();

        assertEquals(id.getValue(), UUID.fromString(id.getValue()).toString());
    }

    @Test
    void givenTwoGeneratedIDs_whenCompare_thenNotBeEqual() {
        assertNotEquals(PartnerID.generate(), PartnerID.generate());
    }

    @Test
    void givenAValue_whenFrom_thenPreserveValue() {
        String value = "cf4361d9-8e93-4c53-9ac5-629680d98469";

        assertEquals(value, PartnerID.from(value).getValue());
    }

    @Test
    void givenANullValue_whenFrom_thenThrowNullPointerException() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> PartnerID.from(null));

        assertEquals("'PartnerID' should not be null", exception.getMessage());
    }
}
