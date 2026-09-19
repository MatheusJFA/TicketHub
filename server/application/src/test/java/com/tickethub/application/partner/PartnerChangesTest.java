package com.tickethub.application.partner;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.tickethub.application.partner.changeaddress.*;
import com.tickethub.application.partner.create.*;
import com.tickethub.application.partner.retrieve.get.*;
import com.tickethub.application.partner.retrieve.list.*;
import com.tickethub.domain.core.partner.*;
import com.tickethub.domain.auth.PasswordHasher;
import com.tickethub.domain.geo.CepLookup;
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.exception.DomainException;

class PartnerChangesTest {
    private static final String VALID_EMAIL = "cinema@domain.com";
    private static final String RAW_PASSWORD = "secret-123";
    private static final String PASSWORD_HASH = "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS";
    private final Address address = Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000");
    private final Address newAddress = Address.create("Rua B", "20", "Sala 1", "Centro", "Sao Paulo", "SP", "Brasil", "01001001");
    private final Partner partner = Partner.create("Cinema Nova", "11222333000181", address, VALID_EMAIL, PASSWORD_HASH);
    private final PartnerGateway gateway = mock(PartnerGateway.class);
    private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
    private final CepLookup cepLookup = mock(CepLookup.class);
    private final String id = partner.getId().getValue();

    @Test
    void requiresAddressOnCreation() {
        assertThrows(DomainException.class, () -> Partner.create("Cinema Nova", "11222333000181", null, VALID_EMAIL, PASSWORD_HASH));
        when(passwordHasher.hash(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);
        final var result = new DefaultCreatePartnerUseCase(gateway, passwordHasher, cepLookup)
                .execute(CreatePartnerCommand.with("Cinema Nova", "11222333000181", null, VALID_EMAIL, RAW_PASSWORD));
        assertEquals("'address' should not be null", result.getLeft().firstError().message());
        verifyNoInteractions(gateway);
    }

    @Test
    void persistsAddressOnCreationAndExposesItInQueries() {
        when(cepLookup.lookup("01001000")).thenReturn(Optional.empty());
        when(passwordHasher.hash(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);
        when(gateway.create(any())).thenAnswer(i -> i.getArgument(0));
        new DefaultCreatePartnerUseCase(gateway, passwordHasher, cepLookup)
                .execute(CreatePartnerCommand.with("Cinema Nova", "11222333000181", address, VALID_EMAIL, RAW_PASSWORD)).getRight();
        verify(gateway).create(argThat(p -> address.equals(p.getAddress())));
        assertEquals(address, GetPartnerOutput.from(partner).address());
        assertEquals(address, ListPartnersOutput.from(partner).address());
    }

    @Test
    void changesOnlyAddress() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.of(partner));
        when(gateway.update(partner)).thenReturn(partner);
        when(cepLookup.lookup("01001001")).thenReturn(Optional.empty());
        final var createdAt = partner.getCreatedAt();
        final var updatedAt = partner.getUpdatedAt();
        assertEquals(id, new DefaultChangePartnerAddressUseCase(gateway, cepLookup)
                .execute(ChangePartnerAddressCommand.with(id, newAddress)).getRight().id());
        assertEquals(newAddress, partner.getAddress());
        assertEquals("Cinema Nova", partner.getName().getValue());
        assertEquals("11222333000181", partner.getCnpj().getValue());
        assertEquals(createdAt, partner.getCreatedAt());
        assertFalse(partner.getUpdatedAt().isBefore(updatedAt));
        verify(gateway).update(partner);
    }

    @Test
    void rejectsInvalidAddressWithoutMutationOrPersistence() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.of(partner));
        final var updatedAt = partner.getUpdatedAt();
        assertEquals("'address' should not be null", new DefaultChangePartnerAddressUseCase(gateway, cepLookup)
                .execute(ChangePartnerAddressCommand.with(id, null)).getLeft().firstError().message());
        assertEquals("11222333000181", partner.getCnpj().getValue());
        assertEquals(address, partner.getAddress());
        assertEquals(updatedAt, partner.getUpdatedAt());
        verify(gateway, never()).update(any());
    }

    @Test
    void reportsMissingPartnerForAddressChange() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.empty());
        assertEquals("Partner not found: " + id, new DefaultChangePartnerAddressUseCase(gateway, cepLookup)
                .execute(ChangePartnerAddressCommand.with(id, newAddress)).getLeft().firstError().message());
        verify(gateway, never()).update(any());
    }

    @Test
    void reportsPersistenceFailureForAddressChange() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.of(partner));
        when(gateway.update(partner)).thenThrow(new IllegalStateException("save failed"));
        when(cepLookup.lookup("01001001")).thenReturn(Optional.empty());
        assertEquals("save failed", new DefaultChangePartnerAddressUseCase(gateway, cepLookup)
                .execute(ChangePartnerAddressCommand.with(id, newAddress)).getLeft().firstError().message());
    }
}
