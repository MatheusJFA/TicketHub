package com.tickethub.domain.core.ticket;

import static java.util.Objects.requireNonNull;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.nio.charset.StandardCharsets;

import com.tickethub.domain.AggregateRoot;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.exception.InvalidTicketSignatureException;
import com.tickethub.domain.exception.TicketAlreadyUsedException;
import com.tickethub.domain.validation.ValidationHandler;

public class Ticket extends AggregateRoot<TicketID> {
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OrderID orderId;
    private final SpotID spotId;
    private final CustomerID customerId;
    private final String code;
    private final String signature;
    private TicketStatus status;

    private Ticket(TicketID id, OrderID orderId, SpotID spotId, CustomerID customerId,
            String code, String signature, TicketStatus status,
            Instant createdAt, Instant updatedAt, Instant deletedAt, String createdBy, String lastModifiedBy) {
        super(id, createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
        this.orderId = orderId;
        this.spotId = spotId;
        this.customerId = customerId;
        this.code = code;
        this.signature = signature;
        this.status = status;
    }

    /**
     * Issues a ticket for a paid order item. The QR payload is
     * {@code ticketId:code:signature}, where the signature is computed over
     * {@code ticketId:code} so scanners reject forged codes.
     */
    public static Ticket issue(final OrderID orderId, final SpotID spotId, final CustomerID customerId,
            final TicketSigner signer) {
        requireNonNull(orderId, "'orderId' should not be null");
        requireNonNull(spotId, "'spotId' should not be null");
        requireNonNull(customerId, "'customerId' should not be null");
        requireNonNull(signer, "'signer' should not be null");
        final var id = TicketID.generate();
        final var code = generateCode();
        final var signature = signer.sign(signedPayload(id.getValue(), code));
        final var now = Instant.now();
        final var ticket = new Ticket(id, orderId, spotId, customerId, code, signature,
                TicketStatus.ISSUED, now, now, null, null, null);
        ticket.registerEvent(new TicketIssued(id.getValue(), orderId.getValue(), now));
        return ticket;
    }

    public static Ticket reconstitute(TicketID id, OrderID orderId, SpotID spotId, CustomerID customerId,
            String code, String signature, TicketStatus status,
            Instant createdAt, Instant updatedAt, Instant deletedAt, String createdBy, String lastModifiedBy) {
        return new Ticket(id, orderId, spotId, customerId, code, signature, status,
                createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
    }

    /**
     * Recomputes the signature and rejects tickets that were not issued by
     * this system (or were tampered with) before any other check.
     */
    public void verifySignature(final TicketSigner signer) {
        requireNonNull(signer, "'signer' should not be null");
        final var expected = signer.sign(signedPayload(getId().getValue(), code));
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8))) {
            throw new InvalidTicketSignatureException();
        }
    }

    /**
     * Marks the ticket as used at check-in. A ticket can only be checked in
     * once; a second scan fails so a ticket cannot be reused.
     */
    public void checkIn() {
        if (status == TicketStatus.USED) {
            throw new TicketAlreadyUsedException();
        }
        this.status = TicketStatus.USED;
        markAsUpdated();
        registerEvent(new TicketCheckedIn(getId().getValue(), orderId.getValue(), Instant.now()));
    }

    public static String signedPayload(final String ticketId, final String code) {
        return ticketId + ":" + code;
    }

    private static String generateCode() {
        final var builder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            builder.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
        }
        return builder.toString();
    }

    @Override
    public void validate(final ValidationHandler handler) {
        new TicketValidator(this, handler).validate();
    }

    public OrderID getOrderId() {
        return orderId;
    }

    public SpotID getSpotId() {
        return spotId;
    }

    public CustomerID getCustomerId() {
        return customerId;
    }

    public String getCode() {
        return code;
    }

    public String getSignature() {
        return signature;
    }

    public TicketStatus getStatus() {
        return status;
    }
}
