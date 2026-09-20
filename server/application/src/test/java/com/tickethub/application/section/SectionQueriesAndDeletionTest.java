package com.tickethub.application.section;

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
import com.tickethub.domain.core.section.*;
import com.tickethub.application.section.retrieve.get.*;
import com.tickethub.application.section.retrieve.list.*;
import com.tickethub.application.section.delete.*;

@DisplayName("Section queries and deletion")
class SectionQueriesAndDeletionTest {
    private final Section entity = Section.create("Setor original", "Descricao original", 2, Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL")), "A", 5);
    private final SectionGateway gateway = mock(SectionGateway.class);
    private final String id = entity.getId().getValue();
    private final SearchQuery query = new SearchQuery(2, 10, "search", "id", "asc");

    @Test
    @DisplayName("Gets entity with all output fields")
    void getsEntityWithAllOutputFields() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        final var output = new DefaultGetSectionUseCase(gateway).execute(id).getRight();
        assertEquals(id, output.id());
        assertEquals(entity.getName().getValue(), output.name());
        assertEquals(entity.getDescription().getValue(), output.description());
        assertEquals(entity.getPrice(), output.price());
        assertEquals(entity.isPublished(), output.published());
        assertEquals(entity.getTotalSpots(), output.totalSpots());
        assertEquals(entity.getTotalSpotsSold(), output.totalSpotsSold());
        assertEquals(entity.getCreatedAt(), output.createdAt());
        assertEquals(entity.getUpdatedAt(), output.updatedAt());
        assertEquals(entity.getDeletedAt(), output.deletedAt());
        verify(gateway).findById(entity.getId());
    }

    @Test
    @DisplayName("Reports missing entity")
    void reportsMissingEntity() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.empty());
        final var result = new DefaultGetSectionUseCase(gateway).execute(id);
        assertEquals("Section not found: " + id, result.getLeft().firstError().message());
    }

    @Test
    @DisplayName("Reports lookup failure")
    void reportsLookupFailure() {
        when(gateway.findById(entity.getId())).thenThrow(new IllegalStateException("lookup failed"));
        final var result = new DefaultGetSectionUseCase(gateway).execute(id);
        assertEquals("lookup failed", result.getLeft().firstError().message());
    }

    @Test
    @DisplayName("Preserves page metadata and maps all fields")
    void preservesPageMetadataAndMapsAllFields() {
        when(gateway.findAll(query)).thenReturn(new Pagination<>(2, 10, 21, List.of(entity)));
        final var page = new DefaultListSectionsUseCase(gateway).execute(query).getRight();
        assertEquals(2, page.currentPage());
        assertEquals(10, page.perPage());
        assertEquals(21, page.totalItems());
        assertEquals(1, page.items().size());
        final var output = page.items().getFirst();
        assertEquals(id, output.id());
        assertEquals(entity.getName().getValue(), output.name());
        assertEquals(entity.getDescription().getValue(), output.description());
        assertEquals(entity.getPrice(), output.price());
        assertEquals(entity.isPublished(), output.published());
        assertEquals(entity.getTotalSpots(), output.totalSpots());
        assertEquals(entity.getTotalSpotsSold(), output.totalSpotsSold());
        verify(gateway).findAll(query);
    }

    @Test
    @DisplayName("Returns empty page")
    void returnsEmptyPage() {
        when(gateway.findAll(query)).thenReturn(new Pagination<>(2, 10, 0, List.of()));
        final var page = new DefaultListSectionsUseCase(gateway).execute(query).getRight();
        assertEquals(0, page.totalItems());
        assertTrue(page.items().isEmpty());
    }

    @Test
    @DisplayName("Reports listing failure")
    void reportsListingFailure() {
        when(gateway.findAll(query)).thenThrow(new IllegalStateException("list failed"));
        final var result = new DefaultListSectionsUseCase(gateway).execute(query);
        assertEquals("list failed", result.getLeft().firstError().message());
    }

    @Test
    @DisplayName("Delegates repeated deletion without lookup")
    void delegatesRepeatedDeletionWithoutLookup() {
        final var useCase = new DefaultDeleteSectionUseCase(gateway);
        assertTrue(useCase.execute(id).isEmpty());
        assertTrue(useCase.execute(id).isEmpty());
        verify(gateway, times(2)).deleteById(entity.getId());
        verifyNoMoreInteractions(gateway);
    }

    @Test
    @DisplayName("Reports deletion failure")
    void reportsDeletionFailure() {
        doThrow(new IllegalStateException("delete failed")).when(gateway).deleteById(entity.getId());
        final var result = new DefaultDeleteSectionUseCase(gateway).execute(id);
        assertEquals("delete failed", result.orElseThrow().firstError().message());
    }
}
