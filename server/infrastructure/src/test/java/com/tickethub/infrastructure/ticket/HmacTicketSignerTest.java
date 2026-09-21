package com.tickethub.infrastructure.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("HMAC ticket signer")
class HmacTicketSignerTest {

    private final HmacTicketSigner signer = new HmacTicketSigner("test-secret-0123456789abcdef");

    @Test
    @DisplayName("Given payload, when signs twice, then returns deterministic signature")
    void givenPayload_whenSignsTwice_thenDeterministic() {
        assertEquals(signer.sign("ticket-1:ABCDEFGH"), signer.sign("ticket-1:ABCDEFGH"));
    }

    @Test
    @DisplayName("Given different payloads, when signs, then returns different signatures")
    void givenDifferentPayloads_whenSigns_thenDifferent() {
        assertNotEquals(signer.sign("ticket-1:ABCDEFGH"), signer.sign("ticket-1:XXXXYYYY"));
    }

    @Test
    @DisplayName("Given blank secret, when creates signer, then throws")
    void givenBlankSecret_whenCreates_thenThrows() {
        assertThrows(IllegalStateException.class, () -> new HmacTicketSigner("  "));
    }
}
