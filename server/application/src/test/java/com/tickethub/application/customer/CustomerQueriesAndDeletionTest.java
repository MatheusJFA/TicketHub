package com.tickethub.application.customer;

import java.util.List;
import java.util.Optional;
import java.util.Currency;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.tickethub.domain.shared.*;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.pagination.*;
import com.tickethub.domain.core.customer.*;
import com.tickethub.application.customer.retrieve.get.*;
import com.tickethub.application.customer.retrieve.list.*;
import com.tickethub.application.customer.delete.*;

class CustomerQueriesAndDeletionTest {
    private final Customer entity = Customer.create("52998224725", "Maria Silva");
    private final CustomerGateway gateway = mock(CustomerGateway.class);
    private final String id = entity.getId().getValue();
    private final SearchQuery query = new SearchQuery(2, 10, "search", "id", "asc");

    @Test
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
    void reportsMissingEntity() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.empty());
        final var result = new DefaultGetCustomerUseCase(gateway).execute(id);
        assertEquals("Customer not found: " + id, result.getLeft().firstError().message());
    }

    @Test
    void reportsLookupFailure() {
        when(gateway.findById(entity.getId())).thenThrow(new IllegalStateException("lookup failed"));
        final var result = new DefaultGetCustomerUseCase(gateway).execute(id);
        assertEquals("lookup failed", result.getLeft().firstError().message());
    }

    @Test
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
    void returnsEmptyPage() {
        when(gateway.findAll(query)).thenReturn(new Pagination<>(2, 10, 0, List.of()));
        final var page = new DefaultListCustomersUseCase(gateway).execute(query).getRight();
        assertEquals(0, page.totalItems());
        assertTrue(page.items().isEmpty());
    }

    @Test
    void reportsListingFailure() {
        when(gateway.findAll(query)).thenThrow(new IllegalStateException("list failed"));
        final var result = new DefaultListCustomersUseCase(gateway).execute(query);
        assertEquals("list failed", result.getLeft().firstError().message());
    }

    @Test
    void delegatesRepeatedDeletionWithoutLookup() {
        final var useCase = new DefaultDeleteCustomerUseCase(gateway);
        assertTrue(useCase.execute(id).isEmpty());
        assertTrue(useCase.execute(id).isEmpty());
        verify(gateway, times(2)).deleteById(entity.getId());
        verifyNoMoreInteractions(gateway);
    }

    @Test
    void reportsDeletionFailure() {
        doThrow(new IllegalStateException("delete failed")).when(gateway).deleteById(entity.getId());
        final var result = new DefaultDeleteCustomerUseCase(gateway).execute(id);
        assertEquals("delete failed", result.orElseThrow().firstError().message());
    }
}
