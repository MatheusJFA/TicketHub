package com.tickethub.domain.core.payment;

import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.shared.Money;

/**
 * Payment provider port. The first adapter is a deterministic mock; a real
 * PSP plugs in here later without touching the domain or the use cases.
 */
public interface PaymentGateway {
    Charge createCharge(OrderID orderId, Money total);

    Charge findStatus(ChargeID chargeId);
    /**
     * Returns captured money for the charge. Callers re-read the charge
     * afterwards: providers confirm the terminal state asynchronously.
     */
    void refund(ChargeID chargeId);
}
