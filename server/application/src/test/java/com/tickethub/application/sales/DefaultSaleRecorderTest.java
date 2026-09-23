package com.tickethub.application.sales;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.order.OrderItem;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.core.spot.SpotPlacement;
import com.tickethub.domain.shared.Location;
import com.tickethub.domain.shared.Money;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Default sale recorder")
class DefaultSaleRecorderTest extends UseCaseTest {

    private final SpotGateway spots = mock(SpotGateway.class);
    private final SectionGateway sections = mock(SectionGateway.class);
    private final ShowGateway shows = mock(ShowGateway.class);
    private final SaleRecorder recorder = new DefaultSaleRecorder(spots, sections, shows);

    @Override
    protected List<Object> getMocks() {
        return List.of(spots, sections, shows);
    }

    private Order givenOrder(final Spot spot, final Section section, final Show show) {
        final var order = Order.create(
                CustomerID.generate(),
                List.of(OrderItem.of(spot.getId(), Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL")))),
                Duration.ofMinutes(15));
        when(spots.findPlacement(spot.getId()))
                .thenReturn(Optional.of(new SpotPlacement(
                        spot, show.getId().getValue(), section.getId().getValue())));
        when(sections.findById(section.getId())).thenReturn(Optional.of(section));
        when(shows.findById(show.getId())).thenReturn(Optional.of(show));
        return order;
    }

    private Show givenShow() {
        return Show.create(
                "Show",
                "Desc",
                java.time.OffsetDateTime.now().plusHours(2),
                null,
                10,
                com.tickethub.domain.core.partner.PartnerID.generate(),
                java.util.Set.of());
    }

    @Test
    @DisplayName("Given order, when record sale, then bumps section and show")
    void givenOrder_whenRecordSale_thenBumpsCounters() {
        final var section = Section.create(
                "VIP", "Front", 10, Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL")), "A", 5);
        final var show = givenShow();
        final var order = givenOrder(Spot.create(Location.create("A1")), section, show);

        recorder.recordSale(order);

        assertEquals(1, section.getTotalSpotsSold());
        assertEquals(1, show.getTotalSpotsSold());
        verify(spots, times(1)).findPlacement(any());
        verify(sections, times(1)).findById(any());
        verify(shows, times(1)).findById(any());
        verify(sections, times(1)).update(section);
        verify(shows, times(1)).update(show);
    }

    @Test
    @DisplayName("Given unknown placement, when record sale, then skips silently")
    void givenUnknownPlacement_whenRecordSale_thenSkips() {
        final var order = Order.create(
                CustomerID.generate(),
                List.of(OrderItem.of(
                        SpotID.generate(), Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL")))),
                Duration.ofMinutes(15));
        when(spots.findPlacement(any())).thenReturn(Optional.empty());

        recorder.recordSale(order);

        verify(spots, times(1)).findPlacement(any());
        verify(sections, never()).update(any());
        verify(shows, never()).update(any());
    }

    @Test
    @DisplayName("Given order, when record refund, then decrements without going negative")
    void givenOrder_whenRecordRefund_thenDecrements() {
        final var section = Section.create(
                "VIP", "Front", 10, Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL")), "A", 5);
        final var show = givenShow();
        final var order = givenOrder(Spot.create(Location.create("A1")), section, show);

        recorder.recordSale(order);
        recorder.recordRefund(order);
        recorder.recordRefund(order);

        assertEquals(0, section.getTotalSpotsSold());
        assertEquals(0, show.getTotalSpotsSold());
        verify(spots, times(3)).findPlacement(any());
        verify(sections, times(3)).findById(any());
        verify(shows, times(3)).findById(any());
        verify(sections, times(3)).update(any());
        verify(shows, times(3)).update(any());
    }
}
