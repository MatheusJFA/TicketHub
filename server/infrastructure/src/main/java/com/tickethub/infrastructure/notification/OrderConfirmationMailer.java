package com.tickethub.infrastructure.notification;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.ticket.TicketGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Sends the buyer their tickets after a confirmed payment. Best effort by
 * design: any failure is logged and never breaks the confirmation itself.
 */
@Component
public class OrderConfirmationMailer {

    private static final Logger LOG = LoggerFactory.getLogger(OrderConfirmationMailer.class);

    private final OrderGateway orders;
    private final CustomerGateway customers;
    private final TicketGateway tickets;
    private final JavaMailSender mailSender;
    private final String from;

    public OrderConfirmationMailer(
            final OrderGateway orders,
            final CustomerGateway customers,
            final TicketGateway tickets,
            final JavaMailSender mailSender,
            @Value("${tickethub.mail.from:noreply@tickethub.local}") final String from) {
        this.orders = requireNonNull(orders, "'orders' should not be null");
        this.customers = requireNonNull(customers, "'customers' should not be null");
        this.tickets = requireNonNull(tickets, "'tickets' should not be null");
        this.mailSender = requireNonNull(mailSender, "'mailSender' should not be null");
        this.from = requireNonNull(from, "'from' should not be null");
    }

    public void sendFor(final String orderId) {
        try {
            final var order = orders.findById(OrderID.from(orderId)).orElse(null);
            if (isNull(order)) {
                return;
            }
            final var email = customers
                    .findById(order.getCustomerId())
                    .map(customer -> customer.getEmail().getValue())
                    .orElse(null);
            if (isNull(email)) {
                LOG.warn("Confirmation email skipped, customer has no email orderId={}", orderId);
                return;
            }
            final var message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(email);
            message.setSubject("Seus ingressos do TicketHub (" + orderId + ")");
            message.setText(body(orderId));
            mailSender.send(message);
            LOG.info("Confirmation email sent orderId={} to={}", orderId, email);
        } catch (final RuntimeException e) {
            LOG.warn("Confirmation email failed orderId={} error={}", orderId, e.getMessage());
        }
    }

    private String body(final String orderId) {
        final var lines = new StringBuilder("Pedido ")
                .append(orderId)
                .append(" confirmado! Apresente um código por ingresso na portaria:\n");
        for (final var ticket : tickets.findByOrderId(OrderID.from(orderId))) {
            lines.append("\n- ").append(ticket.getId().getValue()).append(" · ").append(ticket.getCode());
        }
        return lines.toString();
    }
}
