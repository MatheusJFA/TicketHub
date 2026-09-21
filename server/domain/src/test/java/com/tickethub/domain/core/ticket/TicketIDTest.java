package com.tickethub.domain.core.ticket;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.UUID;

@DisplayName("Ticket ID")
class TicketIDTest {

    @Test
    @DisplayName("Given no value, when generate, then return valid uuid")
    void givenNoValue_whenGenerate_thenReturnValidUUID() {
        TicketID id = TicketID.generate();

        assertEquals(id.getValue(), UUID.fromString(id.getValue()).toString());
    }

    @Test
    @DisplayName("Given two generated ids, when compare, then not be equal")
    void givenTwoGeneratedIDs_whenCompare_thenNotBeEqual() {
        assertNotEquals(TicketID.generate(), TicketID.generate());
    }

    @Test
    @DisplayName("Given a value, when from, then preserve value")
    void givenAValue_whenFrom_thenPreserveValue() {
        String value = "cf4361d9-8e93-4c53-9ac5-629680d98469";

        assertEquals(value, TicketID.from(value).getValue());
    }

    @Test
    @DisplayName("Given a null value, when from, then throw null pointer exception")
    void givenANullValue_whenFrom_thenThrowNullPointerException() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> TicketID.from(null));

        assertEquals("'TicketID' should not be null", exception.getMessage());
    }
}
