package com.tickethub.application.customer;

import java.util.List;
import java.util.Optional;
import java.util.Currency;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.tickethub.domain.shared.*;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.pagination.*;
import com.tickethub.domain.core.customer.*;
import com.tickethub.application.customer.retrieve.get.*;
import com.tickethub.application.customer.retrieve.list.*;
import com.tickethub.application.customer.delete.*;

@DisplayName("Customer queries and deletion")
class CustomerQueriesAndDeletionTest {
    private final Customer entity = Customer.create("52998224725", "Maria Silva", "maria@domain.com",
            "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS");
    private final CustomerGateway gateway = mock(CustomerGateway.class);
    private final String id = entity.getId().getValue();
    private final SearchQuery query = new SearchQuery(2, 10, "search", "id", "asc");

    @Test
    @DisplayName("Gets entity with all output fields")
    void getsEntityWithAllOutputFields() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        final var output = new DefaultGetCustomerUseCase(gateway).execute(id).getRight();
        assertEquals(id, output.id());
        assertEquals(entity.getName().getValue(), output.name());
        assertEquals(entity.getCpf().getValue(), output.cpf());
        assertEquals(entity.getCreatedAt(), output.createdAt());
        assertEquals(entity.getUpdatedAt(), output.updatedAt());
        assertEquals(entity.getDeletedAt(), output.deletedAt());
        verify(gateway).findById(entity.getId());
    }

    @Test
    @DisplayName("Reports missing entity")
    void reportsMissingEntity() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.empty());
        final var result = new DefaultGetCustomerUseCase(gateway).execute(id);
        assertEquals("Customer not found: " + id, result.getLeft().firstError().message());
    }

    @Test
    @DisplayName("Reports lookup failure")
    void reportsLookupFailure() {
        when(gateway.findById(entity.getId())).thenThrow(new IllegalStateException("lookup failed"));
        final var result = new DefaultGetCustomerUseCase(gateway).execute(id);
        assertEquals("lookup failed", result.getLeft().firstError().message());
    }

    @Test
    @DisplayName("Preserves page metadata and maps all fields")
    void preservesPageMetadataAndMapsAllFields() {
        when(gateway.findAll(query)).thenReturn(new Pagination<>(2, 10, 21, List.of(entity)));
        final var page = new DefaultListCustomersUseCase(gateway).execute(query).getRight();
        assertEquals(2, page.currentPage());
        assertEquals(10, page.perPage());
        assertEquals(21, page.totalItems());
        assertEquals(1, page.items().size());
        final var output = page.items().getFirst();
        assertEquals(id, output.id());
        assertEquals(entity.getName().getValue(), output.name());
        assertEquals(entity.getCpf().getValue(), output.cpf());
        verify(gateway).findAll(query);
    }

    @Test
    @DisplayName("Returns empty page")
    void returnsEmptyPage() {
        when(gateway.findAll(query)).thenReturn(new Pagination<>(2, 10, 0, List.of()));
        final var page = new DefaultListCustomersUseCase(gateway).execute(query).getRight();
        assertEquals(0, page.totalItems());
        assertTrue(page.items().isEmpty());
    }

    @Test
    @DisplayName("Reports listing failure")
    void reportsListingFailure() {
        when(gateway.findAll(query)).thenThrow(new IllegalStateException("list failed"));
        final var result = new DefaultListCustomersUseCase(gateway).execute(query);
        assertEquals("list failed", result.getLeft().firstError().message());
    }

    @Test
    @DisplayName("Delegates repeated deletion without lookup")
    void delegatesRepeatedDeletionWithoutLookup() {
        final var useCase = new DefaultDeleteCustomerUseCase(gateway);
        assertTrue(useCase.execute(id).isEmpty());
        assertTrue(useCase.execute(id).isEmpty());
        verify(gateway, times(2)).deleteById(entity.getId());
        verifyNoMoreInteractions(gateway);
    }

    @Test
    @DisplayName("Reports deletion failure")
    void reportsDeletionFailure() {
        doThrow(new IllegalStateException("delete failed")).when(gateway).deleteById(entity.getId());
        final var result = new DefaultDeleteCustomerUseCase(gateway).execute(id);
        assertEquals("delete failed", result.orElseThrow().firstError().message());
    }
}
