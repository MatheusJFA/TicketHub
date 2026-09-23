package com.tickethub.application.sales;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.core.spot.SpotGateway;

/**
 * Keeps the sold counters of shows and sections in sync with ticket
 * issuance. Used by every path that settles (webhook, reconciliation) or
 * refunds an order, so the seat map never shows stale availability totals.
 * Unknown placements are skipped: the counters are informational, the
 * tickets are the source of truth.
 */
public class DefaultSaleRecorder implements SaleRecorder {
    private final SpotGateway spotGateway;
    private final SectionGateway sectionGateway;
    private final ShowGateway showGateway;

    public DefaultSaleRecorder(final SpotGateway spotGateway, final SectionGateway sectionGateway,
            final ShowGateway showGateway) {
        this.spotGateway = requireNonNull(spotGateway, "'spotGateway' should not be null");
        this.sectionGateway = requireNonNull(sectionGateway, "'sectionGateway' should not be null");
        this.showGateway = requireNonNull(showGateway, "'showGateway' should not be null");
    }

    @Override
    public void recordSale(final Order order) {
        requireNonNull(order, "'order' should not be null");
        for (final var item : order.getItems()) {
            final var placement = spotGateway.findPlacement(item.getSpotId()).orElse(null);
            if (placement == null) {
                continue;
            }
            sectionGateway.findById(SectionID.from(placement.sectionId())).ifPresent(section -> {
                section.registerSale();
                sectionGateway.update(section);
            });
            showGateway.findById(ShowID.from(placement.showId())).ifPresent(show -> {
                show.registerSale();
                showGateway.update(show);
            });
        }
    }

    @Override
    public void recordRefund(final Order order) {
        requireNonNull(order, "'order' should not be null");
        for (final var item : order.getItems()) {
            final var placement = spotGateway.findPlacement(item.getSpotId()).orElse(null);
            if (placement == null) {
                continue;
            }
            sectionGateway.findById(SectionID.from(placement.sectionId())).ifPresent(section -> {
                section.registerRefund();
                sectionGateway.update(section);
            });
            showGateway.findById(ShowID.from(placement.showId())).ifPresent(show -> {
                show.registerRefund();
                showGateway.update(show);
            });
        }
    }
}
