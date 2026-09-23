package com.tickethub.application.section.retrieve.byshow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.shared.Money;

@DisplayName("List show sections use case")
class ListShowSectionsUseCaseTest extends UseCaseTest {

    private final SectionGateway sectionGateway = mock(SectionGateway.class);
    private final DefaultListShowSectionsUseCase useCase = new DefaultListShowSectionsUseCase(sectionGateway);

    @Override
    protected List<Object> getMocks() {
        return List.of(sectionGateway);
    }

    @Test
    @DisplayName("Given show sections, when execute, then returns page")
    void givenShowSections_whenExecute_thenReturnsPage() {
        final var section = Section.create("VIP", "Front stage", true, 100, 0,
                Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL")), Set.of());
        final var query = new SearchQuery(0, 10, "", "name", "asc");
        when(sectionGateway.findByShowId(ShowID.from("show-1"), query))
                .thenReturn(new Pagination<>(0, 10, 1, List.of(section)));

        final var output = useCase.execute(ListShowSectionsCommand.with("show-1", query)).getRight();

        assertEquals(1, output.totalItems());
        assertEquals(section.getId().getValue(), output.items().get(0).id());
        verify(sectionGateway, times(1)).findByShowId(ShowID.from("show-1"), query);
    }

    @Test
    @DisplayName("Given gateway failure, when execute, then returns left")
    void givenGatewayFailure_whenExecute_thenReturnsLeft() {
        final var query = new SearchQuery(0, 10, "", "name", "asc");
        when(sectionGateway.findByShowId(any(), any())).thenThrow(new RuntimeException("boom"));

        final var notification =
                useCase.execute(ListShowSectionsCommand.with("show-1", query)).getLeft();

        assertTrue(notification.getErrors().size() == 1);
        verify(sectionGateway, times(1)).findByShowId(ShowID.from("show-1"), query);
    }
}
