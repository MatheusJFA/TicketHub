package com.tickethub.infrastructure.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.order.OrderItem;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.core.ticket.Ticket;
import com.tickethub.domain.core.ticket.TicketGateway;
import com.tickethub.domain.shared.Money;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@DisplayName("Order confirmation mailer")
class OrderConfirmationMailerTest {

    private record Fixture(OrderConfirmationMailer mailer, JavaMailSender sender) {}

    private Fixture fixture(final Order order, final Customer customer, final List<Ticket> tickets) {
        final var orders = mock(OrderGateway.class);
        final var customers = mock(CustomerGateway.class);
        final var gateway = mock(TicketGateway.class);
        final var sender = mock(JavaMailSender.class);
        when(orders.findById(OrderID.from("order-1"))).thenReturn(Optional.ofNullable(order));
        if (order != null) {
            when(customers.findById(order.getCustomerId())).thenReturn(Optional.ofNullable(customer));
        }
        when(gateway.findByOrderId(OrderID.from("order-1"))).thenReturn(tickets);
        return new Fixture(
                new OrderConfirmationMailer(orders, customers, gateway, sender, "noreply@tickethub.local"), sender);
    }

    private Order givenOrder() {
        return Order.create(
                com.tickethub.domain.core.customer.CustomerID.generate(),
                List.of(OrderItem.of(
                        SpotID.generate(), Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL")))),
                java.time.Duration.ofMinutes(15));
    }

    @Test
    @DisplayName("Given paid order, when send, then emails ticket codes")
    void givenPaidOrder_whenSend_thenEmailsTicketCodes() {
        final var order = givenOrder();
        final var customer = Customer.create(
                "52998224725",
                "Maria",
                "maria@domain.com",
                "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS");
        final var ticket = Ticket.issue(
                order.getId(), order.getItems().get(0).getSpotId(), order.getCustomerId(), payload -> "sig");
        final var fixture = fixture(order, customer, List.of(ticket));

        fixture.mailer().sendFor("order-1");

        final var captor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(fixture.sender()).send(captor.capture());
        final var sent = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(sent.getTo()).containsExactly("maria@domain.com");
        org.assertj.core.api.Assertions.assertThat(sent.getText()).contains(ticket.getCode());
    }

    @Test
    @DisplayName("Given mail failure, when send, then does not throw")
    void givenMailFailure_whenSend_thenDoesNotThrow() {
        final var order = givenOrder();
        final var customer = Customer.create(
                "52998224725",
                "Maria",
                "maria@domain.com",
                "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS");
        final var fixture = fixture(order, customer, List.of());
        org.mockito.Mockito.doThrow(new RuntimeException("smtp down"))
                .when(fixture.sender())
                .send(any(SimpleMailMessage.class));

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> fixture.mailer().sendFor("order-1"));
    }

    @Test
    @DisplayName("Given unknown order, when send, then skips without sending")
    void givenUnknownOrder_whenSend_thenSkips() {
        final var fixture = fixture(null, null, List.of());

        fixture.mailer().sendFor("missing");

        verify(fixture.sender(), never()).send(any(SimpleMailMessage.class));
    }
}
