package com.tickethub.application.spot;

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
import com.tickethub.domain.core.spot.*;
import com.tickethub.application.spot.retrieve.get.*;
import com.tickethub.application.spot.retrieve.list.*;
import com.tickethub.application.spot.delete.*;

@DisplayName("Spot queries and deletion")
class SpotQueriesAndDeletionTest {
    private final Spot entity = Spot.create(Location.create("A1"));
    private final SpotGateway gateway = mock(SpotGateway.class);
    private final String id = entity.getId().getValue();
    private final SearchQuery query = new SearchQuery(2, 10, "search", "id", "asc");

    @Test
    @DisplayName("Gets entity with all output fields")
    void getsEntityWithAllOutputFields() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        final var output = new DefaultGetSpotUseCase(gateway).execute(id).getRight();
        assertEquals(id, output.id());
        assertEquals(entity.getLocation(), output.location());
        assertEquals(entity.isAvailable(), output.available());
        assertEquals(entity.isPublished(), output.published());
        assertEquals(entity.getCreatedAt(), output.createdAt());
        assertEquals(entity.getUpdatedAt(), output.updatedAt());
        assertEquals(entity.getDeletedAt(), output.deletedAt());
        verify(gateway).findById(entity.getId());
    }

    @Test
    @DisplayName("Reports missing entity")
    void reportsMissingEntity() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.empty());
        final var result = new DefaultGetSpotUseCase(gateway).execute(id);
        assertEquals("Spot not found: " + id, result.getLeft().firstError().message());
    }

    @Test
    @DisplayName("Reports lookup failure")
    void reportsLookupFailure() {
        when(gateway.findById(entity.getId())).thenThrow(new IllegalStateException("lookup failed"));
        final var result = new DefaultGetSpotUseCase(gateway).execute(id);
        assertEquals("lookup failed", result.getLeft().firstError().message());
    }

    @Test
    @DisplayName("Preserves page metadata and maps all fields")
    void preservesPageMetadataAndMapsAllFields() {
        when(gateway.findAll(query)).thenReturn(new Pagination<>(2, 10, 21, List.of(entity)));
        final var page = new DefaultListSpotsUseCase(gateway).execute(query).getRight();
        assertEquals(2, page.currentPage());
        assertEquals(10, page.perPage());
        assertEquals(21, page.totalItems());
        assertEquals(1, page.items().size());
        final var output = page.items().getFirst();
        assertEquals(id, output.id());
        assertEquals(entity.getLocation(), output.location());
        assertEquals(entity.isAvailable(), output.available());
        assertEquals(entity.isPublished(), output.published());
        verify(gateway).findAll(query);
    }

    @Test
    @DisplayName("Returns empty page")
    void returnsEmptyPage() {
        when(gateway.findAll(query)).thenReturn(new Pagination<>(2, 10, 0, List.of()));
        final var page = new DefaultListSpotsUseCase(gateway).execute(query).getRight();
        assertEquals(0, page.totalItems());
        assertTrue(page.items().isEmpty());
    }

    @Test
    @DisplayName("Reports listing failure")
    void reportsListingFailure() {
        when(gateway.findAll(query)).thenThrow(new IllegalStateException("list failed"));
        final var result = new DefaultListSpotsUseCase(gateway).execute(query);
        assertEquals("list failed", result.getLeft().firstError().message());
    }

    @Test
    @DisplayName("Delegates repeated deletion without lookup")
    void delegatesRepeatedDeletionWithoutLookup() {
        final var useCase = new DefaultDeleteSpotUseCase(gateway);
        assertTrue(useCase.execute(id).isEmpty());
        assertTrue(useCase.execute(id).isEmpty());
        verify(gateway, times(2)).deleteById(entity.getId());
        verifyNoMoreInteractions(gateway);
    }

    @Test
    @DisplayName("Reports deletion failure")
    void reportsDeletionFailure() {
        doThrow(new IllegalStateException("delete failed")).when(gateway).deleteById(entity.getId());
        final var result = new DefaultDeleteSpotUseCase(gateway).execute(id);
        assertEquals("delete failed", result.orElseThrow().firstError().message());
    }
}
