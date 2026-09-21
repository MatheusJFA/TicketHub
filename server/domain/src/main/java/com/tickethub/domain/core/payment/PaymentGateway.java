package com.tickethub.domain.core.payment;

import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.shared.Money;

/**
 * Payment provider port. The first adapter is a deterministic mock; a real
 * PSP plugs in here later without touching the domain or the use cases.
 */
public interface PaymentGateway {
    Charge createCharge(OrderID orderId, Money total);
    Charge findStatus(String chargeId);
}
