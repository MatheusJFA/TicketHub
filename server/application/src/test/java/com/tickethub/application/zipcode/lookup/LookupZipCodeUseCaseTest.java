package com.tickethub.application.zipcode.lookup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.geography.ZipCodeAddress;
import com.tickethub.domain.geography.ZipCodeLookup;

@DisplayName("Lookup ZIP code use case")
class LookupZipCodeUseCaseTest extends UseCaseTest {

    private final ZipCodeLookup zipCodeLookup = mock(ZipCodeLookup.class);
    private final DefaultLookupZipCodeUseCase useCase = new DefaultLookupZipCodeUseCase(zipCodeLookup);

    @Override
    protected List<Object> getMocks() {
        return List.of(zipCodeLookup);
    }

    @Test
    @DisplayName("Given known zip, when execute, then returns address data")
    void givenKnownZip_whenExecute_thenReturnsAddressData() {
        final var address = new ZipCodeAddress("01305000", "Avenida Paulista", "Bela Vista",
                "São Paulo", "SP", "Brasil");
        when(zipCodeLookup.lookup("01305-000")).thenReturn(Optional.of(address));

        final var output = useCase.execute("01305-000").getRight();

        assertNotNull(output);
        assertEquals("01305000", output.zipCode());
        assertEquals("Avenida Paulista", output.street());
        assertEquals("Bela Vista", output.neighborhood());
        assertEquals("São Paulo", output.city());
        assertEquals("SP", output.state());
        assertEquals("Brasil", output.country());
        verify(zipCodeLookup, times(1)).lookup("01305-000");
    }

    @Test
    @DisplayName("Given unknown zip, when execute, then returns not found")
    void givenUnknownZip_whenExecute_thenReturnsNotFound() {
        when(zipCodeLookup.lookup("99999-999")).thenReturn(Optional.empty());

        final var notification = useCase.execute("99999-999").getLeft();

        assertEquals("ZipCodeAddress not found: 99999999", notification.firstError().message());
        verify(zipCodeLookup, times(1)).lookup("99999-999");
    }

    @Test
    @DisplayName("Given malformed zip, when execute, then returns not found without lookup")
    void givenMalformedZip_whenExecute_thenReturnsNotFound() {
        final var notification = useCase.execute("abc").getLeft();

        assertEquals("ZipCodeAddress not found: abc", notification.firstError().message());
        verify(zipCodeLookup, times(0)).lookup(org.mockito.ArgumentMatchers.any());
    }
}
