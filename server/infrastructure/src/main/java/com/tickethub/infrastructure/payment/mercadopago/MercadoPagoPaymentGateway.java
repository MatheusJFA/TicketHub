package com.tickethub.infrastructure.payment.mercadopago;

import static java.util.Objects.requireNonNull;

import java.util.Currency;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.payment.Charge;
import com.tickethub.domain.core.payment.ChargeID;
import com.tickethub.domain.core.payment.ChargeStatus;
import com.tickethub.domain.core.payment.PaymentGateway;
import com.tickethub.domain.shared.Money;
import com.tickethub.infrastructure.exception.InfrastructureException;
import com.tickethub.infrastructure.shared.http.HttpUpstreamException;

/**
 * {@link PaymentGateway} backed by Mercado Pago PIX payments.
 * The MP payment id becomes the {@link ChargeID}; the PIX copy-and-paste code
 * ({@code qr_code}) is exposed as {@link Charge#getPaymentCode()}.
 * Stateless: {@link #findStatus(ChargeID)} rebuilds the charge from the MP
 * response ({@code external_reference} carries the order id).
 */
public class MercadoPagoPaymentGateway implements PaymentGateway {

    private static final Logger LOG = LoggerFactory.getLogger(MercadoPagoPaymentGateway.class);

    private final MercadoPagoClient client;
    private final MercadoPagoProperties properties;
    private final OrderGateway orderGateway;
    private final CustomerGateway customerGateway;

    public MercadoPagoPaymentGateway(final MercadoPagoClient client, final MercadoPagoProperties properties,
            final OrderGateway orderGateway, final CustomerGateway customerGateway) {
        this.client = requireNonNull(client, "'client' should not be null");
        this.properties = requireNonNull(properties, "'properties' should not be null");
        this.orderGateway = requireNonNull(orderGateway, "'orderGateway' should not be null");
        this.customerGateway = requireNonNull(customerGateway, "'customerGateway' should not be null");
    }

    @Override
    public Charge createCharge(final OrderID orderId, final Money total) {
        requireNonNull(orderId, "'orderId' should not be null");
        requireNonNull(total, "'total' should not be null");
        ensureEnabled();
        final var response = client.createPixPayment(total.getValue(),
                "TicketHub order " + orderId.getValue(), orderId.getValue(), resolvePayerEmail(orderId));
        if (response.id() == null) {
            throw new HttpUpstreamException("mercadopago", 200, "Missing payment id");
        }
        return Charge.create(ChargeID.from(String.valueOf(response.id())), orderId, total,
                ChargeStatus.PENDING, response.qrCode());
    }

    @Override
    public Charge findStatus(final ChargeID chargeId) {
        requireNonNull(chargeId, "'chargeId' should not be null");
        ensureEnabled();
        final var response = client.getPayment(chargeId.getValue());
        if (response.externalReference() == null) {
            throw new HttpUpstreamException("mercadopago", 200,
                    "Missing external_reference for payment " + chargeId.getValue());
        }
        final var currency = Currency.getInstance(
                response.currencyId() != null ? response.currencyId() : "BRL");
        final var total = Money.create(response.transactionAmount(), currency);
        return Charge.create(chargeId, OrderID.from(response.externalReference()), total,
                mapStatus(response.status()), response.qrCode());
    }

    private void ensureEnabled() {
        if (!properties.isEnabled()) {
            throw new InfrastructureException("Mercado Pago provider is disabled");
        }
    }

    /**
     * Resolves the buyer's email from the order's customer, falling back to
     * the configured {@code payer-email} when the order or customer is not
     * found. Email resolution is fail-open; the MP call itself stays
     * fail-closed.
     */
    String resolvePayerEmail(final OrderID orderId) {
        try {
            final var order = orderGateway.findById(orderId).orElse(null);
            if (order == null) {
                LOG.warn("Order not found for payer email orderId={}, using fallback", orderId.getValue());
                return properties.getPayerEmail();
            }
            final var customer = customerGateway.findById(order.getCustomerId()).orElse(null);
            if (customer == null || customer.getEmail() == null) {
                LOG.warn("Customer not found for payer email orderId={}, using fallback", orderId.getValue());
                return properties.getPayerEmail();
            }
            return customer.getEmail().getValue();
        } catch (final RuntimeException e) {
            LOG.warn("Payer email lookup failed orderId={} error={}, using fallback",
                    orderId.getValue(), e.getMessage());
            return properties.getPayerEmail();
        }
    }

    static ChargeStatus mapStatus(final String mpStatus) {
        if (mpStatus == null) {
            throw new HttpUpstreamException("mercadopago", 200, "Missing payment status");
        }
        return switch (mpStatus.toLowerCase(Locale.ROOT)) {
            case "approved" -> ChargeStatus.PAID;
            case "rejected", "cancelled", "refunded", "charged_back" -> ChargeStatus.FAILED;
            case "pending", "authorized", "in_process", "in_mediation" -> ChargeStatus.PENDING;
            default -> {
                LOG.warn("Unknown Mercado Pago status status={}", mpStatus);
                throw new HttpUpstreamException("mercadopago", 200,
                        "Unknown payment status: " + mpStatus);
            }
        };
    }
}
