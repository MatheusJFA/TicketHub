package com.tickethub.domain.core.ticket;

/**
 * Signs ticket payloads (HMAC in infrastructure) so door scanners can prove
 * a QR code was issued by this system instead of trusting bare identifiers.
 * Kept as a port here because crypto primitives live outside the domain.
 */
public interface TicketSigner {
    String sign(String payload);
}
