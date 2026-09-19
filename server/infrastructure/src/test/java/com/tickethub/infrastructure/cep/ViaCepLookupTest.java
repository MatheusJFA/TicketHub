package com.tickethub.infrastructure.cep;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ViaCepLookupTest {

    @Mock
    private ViaCepClient client;

    private CepProperties properties;
    private ViaCepLookup lookup;

    @BeforeEach
    void setUp() {
        properties = new CepProperties();
        lookup = new ViaCepLookup(client, properties);
    }

    @Test
    void givenKnownCep_whenLookup_thenReturnsCepAddress() {
        when(client.findByCep("01305000")).thenReturn(Optional.of(new ViaCepClient.ViaCepResponse(
                "01305-000", "Rua Augusta", "", "Centro", "São Paulo", "SP", null)));

        final var address = lookup.lookup("01305-000").orElseThrow();

        assertEquals("01305000", address.zipCode());
        assertEquals("Rua Augusta", address.street());
        assertEquals("Centro", address.neighborhood());
        assertEquals("São Paulo", address.city());
        assertEquals("SP", address.state());
        assertEquals("Brasil", address.country());
    }

    @Test
    void givenUnknownCep_whenLookup_thenReturnsEmpty() {
        when(client.findByCep("00000000")).thenReturn(Optional.of(new ViaCepClient.ViaCepResponse(
                "00000-000", "", "", "", "", "", true)));

        assertTrue(lookup.lookup("00000000").isEmpty());
    }

    @Test
    void givenInvalidZip_whenLookup_thenReturnsEmptyWithoutCallingProvider() {
        assertTrue(lookup.lookup("abc").isEmpty());
        assertTrue(lookup.lookup(null).isEmpty());
    }

    @Test
    void givenDisabledProvider_whenLookup_thenReturnsEmpty() {
        properties.setEnabled(false);

        assertTrue(lookup.lookup("01305-000").isEmpty());
    }

    @Test
    void givenClientFailure_whenLookup_thenReturnsEmpty() {
        when(client.findByCep("01305000")).thenThrow(new IllegalStateException("boom"));

        assertTrue(lookup.lookup("01305-000").isEmpty());
    }
}
