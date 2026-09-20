package com.tickethub.application.partner;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.tickethub.application.partner.changeaddress.*;
import com.tickethub.application.partner.create.*;
import com.tickethub.application.partner.retrieve.get.*;
import com.tickethub.application.partner.retrieve.list.*;
import com.tickethub.domain.core.partner.*;
import com.tickethub.domain.authentication.PasswordHasher;
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.exception.DomainException;

@DisplayName("Partner changes")
class PartnerChangesTest {
    private static final String VALID_EMAIL = "cinema@domain.com";
    private static final String RAW_PASSWORD = "secret-123";
    private static final String PASSWORD_HASH = "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS";
    private final Address address = Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000");
    private final Address newAddress = Address.create("Rua B", "20", "Sala 1", "Centro", "Sao Paulo", "SP", "Brasil", "01001001");
    private final Partner partner = Partner.create("Cinema Nova", "11222333000181", address, VALID_EMAIL, PASSWORD_HASH);
    private final PartnerGateway gateway = mock(PartnerGateway.class);
    private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
    private final String id = partner.getId().getValue();

    @Test
    @DisplayName("Given null address, when create partner, then rejects with address error")
    void requiresAddressOnCreation() {
        final var exception = assertThrows(DomainException.class,
                () -> Partner.create("Cinema Nova", "11222333000181", null, VALID_EMAIL, PASSWORD_HASH),
                () -> "Creating a partner with null address should throw DomainException");

        assertEquals("'address' should not be null", exception.getMessage(),
                () -> "Exception message should indicate that address must not be null");
        when(passwordHasher.hash(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);
        final var result = new DefaultCreatePartnerUseCase(gateway, passwordHasher)
                .execute(CreatePartnerCommand.with("Cinema Nova", "11222333000181", null, VALID_EMAIL, RAW_PASSWORD));
        assertEquals("'address' should not be null", result.getLeft().firstError().message(),
                () -> "Create use case with null address should report address must not be null");
        verifyNoInteractions(gateway);
    }

    @Test
    @DisplayName("Given valid address, when create partner, then persists address and exposes it in queries")
    void persistsAddressOnCreationAndExposesItInQueries() {
        when(passwordHasher.hash(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);
        when(gateway.create(any())).thenAnswer(i -> i.getArgument(0));
        new DefaultCreatePartnerUseCase(gateway, passwordHasher)
                .execute(CreatePartnerCommand.with("Cinema Nova", "11222333000181", address, VALID_EMAIL, RAW_PASSWORD)).getRight();
        verify(gateway).create(argThat(p -> address.equals(p.getAddress())));
        assertEquals(address, GetPartnerOutput.from(partner).address(),
                () -> "Get partner output should expose the persisted address");
        assertEquals(address, ListPartnersOutput.from(partner).address(),
                () -> "List partners output should expose the persisted address");
    }

    @Test
    @DisplayName("Given existing partner, when change address, then updates only address")
    void changesOnlyAddress() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.of(partner));
        when(gateway.update(partner)).thenReturn(partner);
        final var createdAt = partner.getCreatedAt();
        final var updatedAt = partner.getUpdatedAt();
        assertEquals(id, new DefaultChangePartnerAddressUseCase(gateway)
                .execute(ChangePartnerAddressCommand.with(id, newAddress)).getRight().id(),
                () -> "Change address use case should return the partner id");
        assertEquals(newAddress, partner.getAddress(), () -> "Partner should expose the new address");
        assertEquals("Cinema Nova", partner.getName().getValue(), () -> "Changing address should keep the name");
        assertEquals("11222333000181", partner.getCnpj().getValue(), () -> "Changing address should keep the cnpj");
        assertEquals(createdAt, partner.getCreatedAt(), () -> "Changing address should keep createdAt");
        assertFalse(partner.getUpdatedAt().isBefore(updatedAt),
                () -> "Changing address should not move updatedAt backwards");
        verify(gateway).update(partner);
    }

    @Test
    @DisplayName("Given null address, when change address, then rejects without mutation or persistence")
    void rejectsInvalidAddressWithoutMutationOrPersistence() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.of(partner));
        final var updatedAt = partner.getUpdatedAt();
        assertEquals("'address' should not be null", new DefaultChangePartnerAddressUseCase(gateway)
                .execute(ChangePartnerAddressCommand.with(id, null)).getLeft().firstError().message(),
                () -> "Change address with null address should report address must not be null");
        assertEquals("11222333000181", partner.getCnpj().getValue(), () -> "Rejected change should keep the cnpj");
        assertEquals(address, partner.getAddress(), () -> "Rejected change should keep the address");
        assertEquals(updatedAt, partner.getUpdatedAt(), () -> "Rejected change should keep updatedAt");
        verify(gateway, never()).update(any());
    }

    @Test
    @DisplayName("Given missing partner, when change address, then reports partner not found")
    void reportsMissingPartnerForAddressChange() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.empty());
        assertEquals("Partner not found: " + id, new DefaultChangePartnerAddressUseCase(gateway)
                .execute(ChangePartnerAddressCommand.with(id, newAddress)).getLeft().firstError().message(),
                () -> "Change address for missing partner should report partner not found");
        verify(gateway, never()).update(any());
    }

    @Test
    @DisplayName("Given persistence failure, when change address, then reports save failure")
    void reportsPersistenceFailureForAddressChange() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.of(partner));
        when(gateway.update(partner)).thenThrow(new IllegalStateException("save failed"));
        assertEquals("save failed", new DefaultChangePartnerAddressUseCase(gateway)
                .execute(ChangePartnerAddressCommand.with(id, newAddress)).getLeft().firstError().message(),
                () -> "Change address with persistence failure should report save failure");
    }
}
