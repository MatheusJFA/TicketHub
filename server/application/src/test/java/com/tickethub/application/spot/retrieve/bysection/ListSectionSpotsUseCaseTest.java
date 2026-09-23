package com.tickethub.application.spot.retrieve.bysection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.shared.Location;

@DisplayName("List section spots use case")
class ListSectionSpotsUseCaseTest extends UseCaseTest {

    private final SpotGateway spotGateway = mock(SpotGateway.class);
    private final DefaultListSectionSpotsUseCase useCase = new DefaultListSectionSpotsUseCase(spotGateway);

    @Override
    protected List<Object> getMocks() {
        return List.of(spotGateway);
    }

    @Test
    @DisplayName("Given section spots, when execute, then returns page")
    void givenSectionSpots_whenExecute_thenReturnsPage() {
        final var spot = Spot.create(Location.create("A1"));
        spot.publish();
        final var query = new SearchQuery(0, 10, "", "createdAt", "asc");
        when(spotGateway.findBySection(SectionID.from("section-1"), query))
                .thenReturn(new Pagination<>(0, 10, 1, List.of(spot)));

        final var output = useCase.execute(ListSectionSpotsCommand.with("section-1", query)).getRight();

        assertEquals(1, output.totalItems());
        assertEquals(spot.getId().getValue(), output.items().get(0).id());
        verify(spotGateway, times(1)).findBySection(SectionID.from("section-1"), query);
    }

    @Test
    @DisplayName("Given gateway failure, when execute, then returns left")
    void givenGatewayFailure_whenExecute_thenReturnsLeft() {
        final var query = new SearchQuery(0, 10, "", "createdAt", "asc");
        when(spotGateway.findBySection(any(), any())).thenThrow(new RuntimeException("boom"));

        final var notification =
                useCase.execute(ListSectionSpotsCommand.with("section-1", query)).getLeft();

        assertTrue(notification.getErrors().size() == 1);
        verify(spotGateway, times(1)).findBySection(SectionID.from("section-1"), query);
    }
}
