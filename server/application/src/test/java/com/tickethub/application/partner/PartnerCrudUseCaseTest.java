package com.tickethub.application.partner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tickethub.application.partner.changename.ChangePartnerNameCommand;
import com.tickethub.application.partner.changename.DefaultChangePartnerNameUseCase;
import com.tickethub.application.partner.delete.DefaultDeletePartnerUseCase;
import com.tickethub.application.partner.retrieve.get.DefaultGetPartnerUseCase;
import com.tickethub.application.partner.retrieve.list.DefaultListPartnersUseCase;
import com.tickethub.domain.core.partner.*;
import com.tickethub.domain.pagination.*;
import com.tickethub.domain.shared.Address;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Partner crud use case")
class PartnerCrudUseCaseTest {
    private final PartnerGateway gateway = mock(PartnerGateway.class);
    private final Partner partner = Partner.create(
            "Cinema Nova",
            "11222333000181",
            Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"),
            "cinema@domain.com",
            "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS");
    private final String id = partner.getId().getValue();
    private final SearchQuery query = new SearchQuery(2, 10, "Cinema", "name", "asc");

    @Test
    @DisplayName("Retrieves partner with audit fields")
    void retrievesPartnerWithAuditFields() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.of(partner));
        final var output = new DefaultGetPartnerUseCase(gateway).execute(id).getRight();
        assertEquals(id, output.id());
        assertEquals("Cinema Nova", output.name());
        assertEquals("11222333000181", output.cnpj());
        assertEquals(partner.getCreatedAt(), output.createdAt());
        assertEquals(partner.getUpdatedAt(), output.updatedAt());
        assertNull(output.deletedAt());
    }

    @Test
    @DisplayName("Retrieves missing partner as notification")
    void retrievesMissingPartnerAsNotification() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.empty());
        assertEquals(
                "Partner not found: " + id,
                new DefaultGetPartnerUseCase(gateway)
                        .execute(id)
                        .getLeft()
                        .firstError()
                        .message());
    }

    @Test
    @DisplayName("Returns lookup failure as notification")
    void returnsLookupFailureAsNotification() {
        when(gateway.findById(partner.getId())).thenThrow(new IllegalStateException("lookup failed"));
        assertEquals(
                "lookup failed",
                new DefaultGetPartnerUseCase(gateway)
                        .execute(id)
                        .getLeft()
                        .firstError()
                        .message());
    }

    @Test
    @DisplayName("Preserves pagination and search query")
    void preservesPaginationAndSearchQuery() {
        when(gateway.findAll(query)).thenReturn(new Pagination<>(2, 10, 21, List.of(partner)));
        final var output =
                new DefaultListPartnersUseCase(gateway).execute(query).getRight();
        assertEquals(2, output.currentPage());
        assertEquals(10, output.perPage());
        assertEquals(21, output.totalItems());
        assertEquals(id, output.items().getFirst().id());
        assertEquals("Cinema Nova", output.items().getFirst().name());
        verify(gateway).findAll(query);
    }

    @Test
    @DisplayName("Returns empty page")
    void returnsEmptyPage() {
        when(gateway.findAll(query)).thenReturn(new Pagination<>(2, 10, 0, List.of()));
        assertTrue(new DefaultListPartnersUseCase(gateway)
                .execute(query)
                .getRight()
                .items()
                .isEmpty());
    }

    @Test
    @DisplayName("Returns listing failure as notification")
    void returnsListingFailureAsNotification() {
        when(gateway.findAll(query)).thenThrow(new IllegalStateException("list failed"));
        assertEquals(
                "list failed",
                new DefaultListPartnersUseCase(gateway)
                        .execute(query)
                        .getLeft()
                        .firstError()
                        .message());
    }

    @Test
    @DisplayName("Updates partner preserving identity and creation date")
    void updatesPartnerPreservingIdentityAndCreationDate() {
        final var createdAt = partner.getCreatedAt();
        final var updatedAt = partner.getUpdatedAt();
        when(gateway.findById(partner.getId())).thenReturn(Optional.of(partner));
        when(gateway.update(partner)).thenAnswer(invocation -> invocation.getArgument(0));
        final var output = new DefaultChangePartnerNameUseCase(gateway)
                .execute(ChangePartnerNameCommand.with(id, "Cinema Atualizado"))
                .getRight();
        assertEquals(id, output.id());
        assertEquals("Cinema Atualizado", partner.getName().getValue());
        assertEquals("11222333000181", partner.getCnpj().getValue());
        assertEquals(createdAt, partner.getCreatedAt());
        assertFalse(partner.getUpdatedAt().isBefore(updatedAt));
        verify(gateway).update(partner);
    }

    @Test
    @DisplayName("Rejects invalid update without partial mutation or persistence")
    void rejectsInvalidUpdateWithoutPartialMutationOrPersistence() {
        final var updatedAt = partner.getUpdatedAt();
        when(gateway.findById(partner.getId())).thenReturn(Optional.of(partner));
        final var result = new DefaultChangePartnerNameUseCase(gateway).execute(ChangePartnerNameCommand.with(id, "J"));
        assertEquals("Invalid name J", result.getLeft().firstError().message());
        assertEquals("Cinema Nova", partner.getName().getValue());
        assertEquals(updatedAt, partner.getUpdatedAt());
        verify(gateway, never()).update(any());
    }

    @Test
    @DisplayName("Rejects missing partner update")
    void rejectsMissingPartnerUpdate() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.empty());
        final var result =
                new DefaultChangePartnerNameUseCase(gateway).execute(ChangePartnerNameCommand.with(id, "Changed"));
        assertEquals("Partner not found: " + id, result.getLeft().firstError().message());
        verify(gateway, never()).update(any());
    }

    @Test
    @DisplayName("Returns update failure as notification")
    void returnsUpdateFailureAsNotification() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.of(partner));
        when(gateway.update(partner)).thenThrow(new IllegalStateException("update failed"));
        final var result =
                new DefaultChangePartnerNameUseCase(gateway).execute(ChangePartnerNameCommand.with(id, "Changed"));
        assertEquals("update failed", result.getLeft().firstError().message());
    }

    @Test
    @DisplayName("Deletes idempotently through gateway")
    void deletesIdempotentlyThroughGateway() {
        final var useCase = new DefaultDeletePartnerUseCase(gateway);
        assertTrue(useCase.execute(id).isEmpty());
        assertTrue(useCase.execute(id).isEmpty());
        verify(gateway, times(2)).deleteById(partner.getId());
        verifyNoMoreInteractions(gateway);
    }

    @Test
    @DisplayName("Returns delete failure as notification")
    void returnsDeleteFailureAsNotification() {
        doThrow(new IllegalStateException("delete failed")).when(gateway).deleteById(partner.getId());
        assertEquals(
                "delete failed",
                new DefaultDeletePartnerUseCase(gateway)
                        .execute(id)
                        .orElseThrow()
                        .firstError()
                        .message());
    }
}
