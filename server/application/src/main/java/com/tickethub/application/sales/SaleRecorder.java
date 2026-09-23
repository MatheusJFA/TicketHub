package com.tickethub.application.sales;

import com.tickethub.domain.core.order.Order;

public interface SaleRecorder {
    void recordSale(Order order);

    void recordRefund(Order order);
}
