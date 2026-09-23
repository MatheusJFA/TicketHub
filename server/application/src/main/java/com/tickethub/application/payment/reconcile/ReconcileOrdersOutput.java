package com.tickethub.application.payment.reconcile;

import java.util.List;

public record ReconcileOrdersOutput(List<String> settled, List<String> refunded, List<String> expired) {

    public static ReconcileOrdersOutput from(
            final List<String> settled, final List<String> refunded, final List<String> expired) {
        return new ReconcileOrdersOutput(List.copyOf(settled), List.copyOf(refunded), List.copyOf(expired));
    }

    public boolean isEmpty() {
        return settled.isEmpty() && refunded.isEmpty() && expired.isEmpty();
    }
}
