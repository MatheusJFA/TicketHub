package com.tickethub.infrastructure.sales;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.tickethub.application.sales.SaleRecorder;
import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.event.DomainEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Publishing sale recorder")
class PublishingSaleRecorderTest {

    private final SaleRecorder delegate = mock(SaleRecorder.class);
    private final DomainEventPublisher publisher = mock(DomainEventPublisher.class);
    private final PublishingSaleRecorder recorder = new PublishingSaleRecorder(delegate, publisher);
    private final Order order = mock(Order.class);

    @Test
    @DisplayName("Given sale, when record, then delegates and publishes")
    void givenSale_whenRecord_thenDelegatesAndPublishes() {
        recorder.recordSale(order);

        verify(delegate).recordSale(order);
        verify(order).publishDomainEvents(publisher);
    }

    @Test
    @DisplayName("Given refund, when record, then delegates and publishes")
    void givenRefund_whenRecord_thenDelegatesAndPublishes() {
        recorder.recordRefund(order);

        verify(delegate).recordRefund(order);
        verify(order).publishDomainEvents(publisher);
    }
}
