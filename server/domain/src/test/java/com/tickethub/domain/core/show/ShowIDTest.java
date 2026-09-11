package com.tickethub.domain.core.show;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class ShowIDTest {

    @Test
    void givenNoValue_whenGenerate_thenReturnValidUUID() {
        ShowID id = ShowID.generate();

        assertEquals(id.getValue(), UUID.fromString(id.getValue()).toString());
    }

    @Test
    void givenTwoGeneratedIDs_whenCompare_thenNotBeEqual() {
        assertNotEquals(ShowID.generate(), ShowID.generate());
    }

    @Test
    void givenAValue_whenFrom_thenPreserveValue() {
        String value = "cf4361d9-8e93-4c53-9ac5-629680d98469";

        assertEquals(value, ShowID.from(value).getValue());
    }

    @Test
    void givenANullValue_whenFrom_thenThrowNullPointerException() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> ShowID.from(null));

        assertEquals("'ShowID' should not be null", exception.getMessage());
    }
}
