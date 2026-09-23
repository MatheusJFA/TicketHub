package com.tickethub.domain.core.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.exception.IllegalChargeTransitionException;
import com.tickethub.domain.shared.Money;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Charge")
class ChargeTest {

    private static Charge pending() {
        return Charge.create(
                ChargeID.from("ch_123"),
                OrderID.generate(),
                Money.create(BigDecimal.TEN, Currency.getInstance("BRL")),
                ChargeStatus.PENDING,
                "PIX-MOCK-ch_123");
    }

    @ParameterizedTest(name = "Given pending charge, when change status to {0}, then allowed={1}")
    @CsvSource({"PAID, true", "FAILED, true", "PENDING, false"})
    @DisplayName("Given pending charge, when change status, then follow state machine")
    void givenPendingCharge_whenChangeStatus_thenFollowStateMachine(final ChargeStatus target, final boolean allowed) {
        final var charge = pending();

        if (allowed) {
            charge.changeStatus(target);

            assertEquals(target, charge.getStatus());
        } else {
            final var exception =
                    assertThrows(IllegalChargeTransitionException.class, () -> charge.changeStatus(target));

            assertEquals("Illegal charge transition from PENDING to " + target, exception.getMessage());
            assertEquals(ChargeStatus.PENDING, charge.getStatus());
        }
    }

    @ParameterizedTest(name = "Given terminal charge {0}, when change status to {1}, then rejected")
    @CsvSource({"PAID, PAID", "PAID, FAILED", "PAID, PENDING", "FAILED, PAID", "FAILED, FAILED", "FAILED, PENDING"})
    @DisplayName("Given terminal charge, when change status, then reject every transition")
    void givenTerminalCharge_whenChangeStatus_thenReject(final ChargeStatus from, final ChargeStatus target) {
        final var charge = pending();
        charge.changeStatus(from);

        final var exception = assertThrows(IllegalChargeTransitionException.class, () -> charge.changeStatus(target));

        assertEquals("Illegal charge transition from " + from + " to " + target, exception.getMessage());
        assertEquals(from, charge.getStatus());
    }

    @Test
    @DisplayName("Given pending charge, when mark as paid or failed, then move once")
    void givenPendingCharge_whenMark_thenMoveOnce() {
        final var paid = pending();
        paid.markAsPaid();
        assertEquals(ChargeStatus.PAID, paid.getStatus());

        final var failed = pending();
        failed.markAsFailed();
        assertEquals(ChargeStatus.FAILED, failed.getStatus());
    }

    @Test
    @DisplayName("Given null status, when change status, then throw null pointer exception")
    void givenNullStatus_whenChangeStatus_thenThrowNullPointer() {
        assertThrows(NullPointerException.class, () -> pending().changeStatus(null));
    }

    @Test
    @DisplayName("Given pending charge, when check transitions, then only paid and failed allowed")
    void givenPendingCharge_whenCheckTransitions_thenOnlyTerminalAllowed() {
        assertTrue(ChargeStatus.PENDING.canTransitionTo(ChargeStatus.PAID));
        assertTrue(ChargeStatus.PENDING.canTransitionTo(ChargeStatus.FAILED));
        assertFalse(ChargeStatus.PENDING.canTransitionTo(ChargeStatus.PENDING));
        assertFalse(ChargeStatus.PAID.canTransitionTo(ChargeStatus.PAID));
        assertFalse(ChargeStatus.FAILED.canTransitionTo(ChargeStatus.PENDING));
    }
}
