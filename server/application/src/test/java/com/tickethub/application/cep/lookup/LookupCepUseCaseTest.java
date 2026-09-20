package com.tickethub.application.cep.lookup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.geo.CepAddress;
import com.tickethub.domain.geo.CepLookup;

@DisplayName("Lookup CEP use case")
class LookupCepUseCaseTest extends UseCaseTest {

    private final CepLookup cepLookup = org.mockito.Mockito.mock(CepLookup.class);
    private final DefaultLookupCepUseCase useCase = new DefaultLookupCepUseCase(cepLookup);

    @Override
    protected List<Object> getMocks() {
        return List.of(cepLookup);
    }

    @Test
    @DisplayName("Given known zip, when execute, then returns address data")
    void givenKnownZip_whenExecute_thenReturnsAddressData() {
        final var address = new CepAddress("01305000", "Avenida Paulista", "Bela Vista",
                "São Paulo", "SP", "Brasil");
        when(cepLookup.lookup("01305-000")).thenReturn(Optional.of(address));

        final var output = useCase.execute("01305-000").getRight();

        assertNotNull(output);
        assertEquals("01305000", output.zipCode());
        assertEquals("Avenida Paulista", output.street());
        assertEquals("Bela Vista", output.neighborhood());
        assertEquals("São Paulo", output.city());
        assertEquals("SP", output.state());
        assertEquals("Brasil", output.country());
        verify(cepLookup, times(1)).lookup("01305-000");
    }

    @Test
    @DisplayName("Given unknown zip, when execute, then returns not found")
    void givenUnknownZip_whenExecute_thenReturnsNotFound() {
        when(cepLookup.lookup("99999-999")).thenReturn(Optional.empty());

        final var notification = useCase.execute("99999-999").getLeft();

        assertEquals("CepAddress not found: 99999999", notification.firstError().message());
        verify(cepLookup, times(1)).lookup("99999-999");
    }

    @Test
    @DisplayName("Given malformed zip, when execute, then returns not found without lookup")
    void givenMalformedZip_whenExecute_thenReturnsNotFound() {
        final var notification = useCase.execute("abc").getLeft();

        assertEquals("CepAddress not found: abc", notification.firstError().message());
        verify(cepLookup, times(0)).lookup(org.mockito.ArgumentMatchers.any());
    }
}
