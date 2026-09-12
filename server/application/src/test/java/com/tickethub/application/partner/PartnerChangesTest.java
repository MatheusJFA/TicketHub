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
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.exception.DomainException;

class PartnerChangesTest {
    private final Address address = Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000");
    private final Address newAddress = Address.create("Rua B", "20", "Sala 1", "Centro", "Sao Paulo", "SP", "Brasil", "01001001");
    private final Partner partner = Partner.create("Cinema Nova", "11222333000181", address);
    private final PartnerGateway gateway = mock(PartnerGateway.class);
    private final String id = partner.getId().getValue();

    @Test
    void requiresAddressOnCreation() {
        assertThrows(DomainException.class, () -> Partner.create("Cinema Nova", "11222333000181", null));
        final var result = new DefaultCreatePartnerUseCase(gateway)
                .execute(CreatePartnerCommand.with("Cinema Nova", "11222333000181", null));
        assertEquals("'address' should not be null", result.getLeft().firstError().message());
        verifyNoInteractions(gateway);
    }

    @Test
    void persistsAddressOnCreationAndExposesItInQueries() {
        when(gateway.create(any())).thenAnswer(i -> i.getArgument(0));
        new DefaultCreatePartnerUseCase(gateway)
                .execute(CreatePartnerCommand.with("Cinema Nova", "11222333000181", address)).getRight();
        verify(gateway).create(argThat(p -> address.equals(p.getAddress())));
        assertEquals(address, GetPartnerOutput.from(partner).address());
        assertEquals(address, ListPartnersOutput.from(partner).address());
    }

    @Test
    void changesOnlyAddress() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.of(partner));
        when(gateway.update(partner)).thenReturn(partner);
        final var createdAt = partner.getCreatedAt();
        final var updatedAt = partner.getUpdatedAt();
        assertEquals(id, new DefaultChangePartnerAddressUseCase(gateway)
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
        assertEquals("'address' should not be null", new DefaultChangePartnerAddressUseCase(gateway)
                .execute(ChangePartnerAddressCommand.with(id, null)).getLeft().firstError().message());
        assertEquals("11222333000181", partner.getCnpj().getValue());
        assertEquals(address, partner.getAddress());
        assertEquals(updatedAt, partner.getUpdatedAt());
        verify(gateway, never()).update(any());
    }

    @Test
    void reportsMissingPartnerForAddressChange() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.empty());
        assertEquals("Partner not found: " + id, new DefaultChangePartnerAddressUseCase(gateway)
                .execute(ChangePartnerAddressCommand.with(id, newAddress)).getLeft().firstError().message());
        verify(gateway, never()).update(any());
    }

    @Test
    void reportsPersistenceFailureForAddressChange() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.of(partner));
        when(gateway.update(partner)).thenThrow(new IllegalStateException("save failed"));
        assertEquals("save failed", new DefaultChangePartnerAddressUseCase(gateway)
                .execute(ChangePartnerAddressCommand.with(id, newAddress)).getLeft().firstError().message());
    }
}
