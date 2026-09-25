package com.tickethub.infrastructure.sales;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.sales.SaleRecorder;
import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.event.DomainEventPublisher;

public class PublishingSaleRecorder implements SaleRecorder {

    private final SaleRecorder delegate;
    private final DomainEventPublisher publisher;

    public PublishingSaleRecorder(final SaleRecorder delegate, final DomainEventPublisher publisher) {
        this.delegate = requireNonNull(delegate, "'delegate' should not be null");
        this.publisher = requireNonNull(publisher, "'publisher' should not be null");
    }

    @Override
    public void recordSale(final Order order) {
        delegate.recordSale(order);
        order.publishDomainEvents(publisher);
    }

    @Override
    public void recordRefund(final Order order) {
        delegate.recordRefund(order);
        order.publishDomainEvents(publisher);
    }
}
