package com.tickethub.application.order.create;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.core.coupon.Coupon;
import com.tickethub.domain.core.coupon.CouponGateway;
import com.tickethub.domain.core.coupon.CouponKind;
import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.order.OrderItem;
import com.tickethub.domain.core.order.OrderStatus;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.core.spot.SpotPlacement;
import com.tickethub.domain.shared.Location;
import com.tickethub.domain.shared.Money;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Create order use case")
class CreateOrderUseCaseTest extends UseCaseTest {

    private static final Currency BRL = Currency.getInstance("BRL");
    private static final Duration TTL = Duration.ofMinutes(15);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-20T12:00:00Z"), ZoneOffset.UTC);

    private final CustomerGateway customerGateway = mock(CustomerGateway.class);
    private final SpotGateway spotGateway = mock(SpotGateway.class);
    private final SectionGateway sectionGateway = mock(SectionGateway.class);
    private final OrderGateway orderGateway = mock(OrderGateway.class);
    private final CouponGateway couponGateway = mock(CouponGateway.class);
    private final DefaultCreateOrderUseCase useCase =
            new DefaultCreateOrderUseCase(customerGateway, spotGateway, sectionGateway, orderGateway, TTL, CLOCK);
    private final DefaultCreateOrderUseCase couponUseCase = new DefaultCreateOrderUseCase(
            customerGateway, spotGateway, sectionGateway, orderGateway, couponGateway, TTL, CLOCK);

    @Override
    protected List<Object> getMocks() {
        return List.of(customerGateway, spotGateway, sectionGateway, orderGateway, couponGateway);
    }

    private Customer givenCustomer() {
        final var customer = Customer.create(
                "52998224725",
                "Maria",
                "maria@domain.com",
                "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS");
        when(customerGateway.findById(customer.getId())).thenReturn(Optional.of(customer));
        return customer;
    }

    private Spot givenFreeSpot(final Section section, final String location) {
        final var spot = Spot.create(Location.create(location));
        spot.publish();
        when(spotGateway.findPlacement(spot.getId()))
                .thenReturn(Optional.of(
                        new SpotPlacement(spot, "show-1", section.getId().getValue())));
        when(spotGateway.reserveIfAvailable(spot.getId())).thenReturn(Optional.of(spot));
        return spot;
    }

    private Section givenSection(final Money price) {
        final var section = Section.create("VIP", "Front stage", true, 100, 0, price, Set.of());
        when(sectionGateway.findById(section.getId())).thenReturn(Optional.of(section));
        return section;
    }

    @Test
    @DisplayName("Given customer and free spots, when execute, then opens pending order")
    void givenCustomerAndFreeSpots_whenExecute_thenOpensPendingOrder() {
        final var customer = givenCustomer();
        final var price = Money.create(new BigDecimal("50.00"), BRL);
        final var section = givenSection(price);
        final var first = givenFreeSpot(section, "A1");
        final var second = givenFreeSpot(section, "A2");
        when(orderGateway.create(any())).thenAnswer(returnsFirstArg());

        final var output = useCase.execute(CreateOrderCommand.with(
                        customer.getId().getValue(),
                        List.of(first.getId().getValue(), second.getId().getValue())))
                .getRight();

        assertNotNull(output.orderId());
        assertEquals(customer.getId().getValue(), output.customerId());
        assertEquals(OrderStatus.PENDING.name(), output.status());
        assertEquals(new BigDecimal("100.00"), output.totalValue());
        assertEquals("BRL", output.currency());
        assertEquals(CLOCK.instant().plus(TTL), output.expiresAt());
        assertEquals(2, output.spotIds().size());
        verify(customerGateway, times(1)).findById(customer.getId());
        verify(spotGateway, times(2)).findPlacement(any());
        verify(spotGateway, times(2)).reserveIfAvailable(any());
        verify(sectionGateway, times(2)).findById(any());
        verify(orderGateway, times(1)).create(any());
    }

    @Test
    @DisplayName("Given unknown customer, when execute, then returns not found")
    void givenUnknownCustomer_whenExecute_thenReturnsNotFound() {
        final var customerId = CustomerID.generate();
        when(customerGateway.findById(customerId)).thenReturn(Optional.empty());

        final var notification = useCase.execute(CreateOrderCommand.with(customerId.getValue(), List.of("spot-1")))
                .getLeft();

        assertEquals(
                "Customer not found: " + customerId.getValue(),
                notification.firstError().message());
        verify(customerGateway, times(1)).findById(customerId);
        verify(spotGateway, times(0)).reserveIfAvailable(any());
        verify(orderGateway, times(0)).create(any());
    }

    @Test
    @DisplayName("Given unknown spot, when execute, then returns not found")
    void givenUnknownSpot_whenExecute_thenReturnsNotFound() {
        final var customer = givenCustomer();
        final var spotId = SpotID.generate();
        when(spotGateway.findPlacement(spotId)).thenReturn(Optional.empty());

        final var notification = useCase.execute(
                        CreateOrderCommand.with(customer.getId().getValue(), List.of(spotId.getValue())))
                .getLeft();

        assertEquals(
                "Spot not found: " + spotId.getValue(),
                notification.firstError().message());
        verify(customerGateway, times(1)).findById(customer.getId());
        verify(spotGateway, times(1)).findPlacement(spotId);
        verify(orderGateway, times(0)).create(any());
    }

    @Test
    @DisplayName("Given taken spot, when execute, then returns unavailable without order")
    void givenTakenSpot_whenExecute_thenReturnsUnavailable() {
        final var customer = givenCustomer();
        final var section = givenSection(Money.create(new BigDecimal("50.00"), BRL));
        final var spot = Spot.create(Location.create("A1"));
        spot.publish();
        when(spotGateway.findPlacement(spot.getId()))
                .thenReturn(Optional.of(
                        new SpotPlacement(spot, "show-1", section.getId().getValue())));
        when(spotGateway.reserveIfAvailable(spot.getId())).thenReturn(Optional.empty());

        final var notification = useCase.execute(CreateOrderCommand.with(
                        customer.getId().getValue(), List.of(spot.getId().getValue())))
                .getLeft();

        assertEquals("Spot is unavailable", notification.firstError().message());
        verify(customerGateway, times(1)).findById(customer.getId());
        verify(spotGateway, times(1)).findPlacement(spot.getId());
        verify(spotGateway, times(1)).reserveIfAvailable(spot.getId());
        verify(orderGateway, times(0)).create(any());
    }

    @Test
    @DisplayName("Given failure on second spot, when execute, then releases first reservation")
    void givenFailureOnSecondSpot_whenExecute_thenReleasesFirst() {
        final var customer = givenCustomer();
        final var section = givenSection(Money.create(new BigDecimal("50.00"), BRL));
        final var first = givenFreeSpot(section, "A1");
        final var missing = SpotID.generate();
        when(spotGateway.findPlacement(missing)).thenReturn(Optional.empty());
        when(spotGateway.update(any())).thenAnswer(returnsFirstArg());

        final var notification = useCase.execute(CreateOrderCommand.with(
                        customer.getId().getValue(), List.of(first.getId().getValue(), missing.getValue())))
                .getLeft();

        assertEquals(
                "Spot not found: " + missing.getValue(),
                notification.firstError().message());
        verify(orderGateway, times(0)).create(any());
        verify(customerGateway, times(1)).findById(customer.getId());
        verify(spotGateway, times(2)).findPlacement(any());
        verify(spotGateway, times(1)).reserveIfAvailable(any());
        verify(sectionGateway, times(1)).findById(any());
        verify(spotGateway, times(1)).update(any());
        assertFalse(first.isReserved());
    }

    @Test
    @DisplayName("Given known idempotency key, when execute, then replays original order")
    void givenKnownKey_whenExecute_thenReplaysOriginalOrder() {
        final var customer = givenCustomer();
        final var price = Money.create(new BigDecimal("50.00"), BRL);
        final var original =
                Order.create(customer.getId(), List.of(OrderItem.of(SpotID.generate(), price)), TTL, CLOCK, "key-1");
        when(orderGateway.findByIdempotencyKey("key-1")).thenReturn(Optional.of(original));

        final var output = useCase.execute(
                        CreateOrderCommand.with(customer.getId().getValue(), List.of("spot-9"), "key-1"))
                .getRight();

        assertEquals(original.getId().getValue(), output.orderId());
        verify(orderGateway, times(1)).findByIdempotencyKey("key-1");
        verify(customerGateway, times(0)).findById(any());
        verify(spotGateway, times(0)).reserveIfAvailable(any());
        verify(orderGateway, times(0)).create(any());
    }

    @Test
    @DisplayName("Given concurrent retry, when insert conflicts, then replays winner")
    void givenConcurrentRetry_whenInsertConflicts_thenReplaysWinner() {
        final var customer = givenCustomer();
        final var price = Money.create(new BigDecimal("50.00"), BRL);
        final var section = givenSection(price);
        final var spot = givenFreeSpot(section, "A1");
        final var winner =
                Order.create(customer.getId(), List.of(OrderItem.of(spot.getId(), price)), TTL, CLOCK, "key-1");
        when(orderGateway.findByIdempotencyKey("key-1"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(winner));
        when(orderGateway.create(any())).thenThrow(new RuntimeException("duplicate key"));

        final var output = useCase.execute(CreateOrderCommand.with(
                        customer.getId().getValue(), List.of(spot.getId().getValue()), "key-1"))
                .getRight();

        assertEquals(winner.getId().getValue(), output.orderId());
        verify(orderGateway, times(2)).findByIdempotencyKey("key-1");
        verify(orderGateway, times(1)).create(any());
        verify(customerGateway, times(1)).findById(customer.getId());
        verify(spotGateway, times(1)).findPlacement(spot.getId());
        verify(spotGateway, times(1)).reserveIfAvailable(spot.getId());
        verify(sectionGateway, times(1)).findById(section.getId());
    }

    private Coupon givenCoupon(final Section section) {
        final var coupon = Coupon.create(
                "PISTA10", null, section.getId().getValue(), CouponKind.PERCENT, 10, null, null, null, null);
        when(couponGateway.findByCode("PISTA10")).thenReturn(Optional.of(coupon));
        when(couponGateway.claimUse("PISTA10", CLOCK.instant())).thenReturn(Optional.of(coupon));
        return coupon;
    }

    @Test
    @DisplayName("Given percent coupon, when execute, then discounts eligible items")
    void givenPercentCoupon_whenExecute_thenDiscountsEligibleItems() {
        final var customer = givenCustomer();
        final var section = givenSection(Money.create(new BigDecimal("50.00"), BRL));
        final var first = givenFreeSpot(section, "A1");
        final var second = givenFreeSpot(section, "A2");
        givenCoupon(section);
        when(orderGateway.create(any())).thenAnswer(returnsFirstArg());

        final var output = couponUseCase
                .execute(CreateOrderCommand.with(
                        customer.getId().getValue(),
                        List.of(first.getId().getValue(), second.getId().getValue()),
                        null,
                        "pista10"))
                .getRight();

        assertEquals(new BigDecimal("90.00"), output.totalValue());
        verify(customerGateway, times(1)).findById(customer.getId());
        verify(spotGateway, times(2)).findPlacement(any());
        verify(spotGateway, times(2)).reserveIfAvailable(any());
        verify(sectionGateway, times(2)).findById(any());
        verify(couponGateway, times(1)).findByCode("PISTA10");
        verify(couponGateway, times(1)).claimUse("PISTA10", CLOCK.instant());
        verify(orderGateway, times(1)).create(any());
    }

    @Test
    @DisplayName("Given fixed coupon, when execute, then discounts subtotal")
    void givenFixedCoupon_whenExecute_thenDiscountsSubtotal() {
        final var customer = givenCustomer();
        final var section = givenSection(Money.create(new BigDecimal("50.00"), BRL));
        final var first = givenFreeSpot(section, "A1");
        final var second = givenFreeSpot(section, "A2");
        final var coupon = Coupon.create(
                "FIX30",
                null,
                section.getId().getValue(),
                CouponKind.FIXED,
                null,
                Money.create(new BigDecimal("30.00"), BRL),
                null,
                null,
                null);
        when(couponGateway.findByCode("FIX30")).thenReturn(Optional.of(coupon));
        when(couponGateway.claimUse("FIX30", CLOCK.instant())).thenReturn(Optional.of(coupon));
        when(orderGateway.create(any())).thenAnswer(returnsFirstArg());

        final var output = couponUseCase
                .execute(CreateOrderCommand.with(
                        customer.getId().getValue(),
                        List.of(first.getId().getValue(), second.getId().getValue()),
                        null,
                        "FIX30"))
                .getRight();

        assertEquals(new BigDecimal("70.00"), output.totalValue());
        verify(customerGateway, times(1)).findById(customer.getId());
        verify(spotGateway, times(2)).findPlacement(any());
        verify(spotGateway, times(2)).reserveIfAvailable(any());
        verify(sectionGateway, times(2)).findById(any());
        verify(couponGateway, times(1)).findByCode("FIX30");
        verify(couponGateway, times(1)).claimUse("FIX30", CLOCK.instant());
        verify(orderGateway, times(1)).create(any());
    }

    @Test
    @DisplayName("Given unknown coupon, when execute, then returns error without order")
    void givenUnknownCoupon_whenExecute_thenReturnsErrorWithoutOrder() {
        final var customer = givenCustomer();
        final var section = givenSection(Money.create(new BigDecimal("50.00"), BRL));
        final var spot = givenFreeSpot(section, "A1");
        when(couponGateway.findByCode("NOPE")).thenReturn(Optional.empty());

        final var notification = couponUseCase
                .execute(CreateOrderCommand.with(
                        customer.getId().getValue(), List.of(spot.getId().getValue()), null, "NOPE"))
                .getLeft();

        assertEquals(
                "Coupon is unknown, expired or exhausted: NOPE",
                notification.firstError().message());
        verify(customerGateway, times(1)).findById(customer.getId());
        verify(spotGateway, times(1)).findPlacement(spot.getId());
        verify(spotGateway, times(1)).reserveIfAvailable(spot.getId());
        verify(sectionGateway, times(1)).findById(section.getId());
        verify(couponGateway, times(1)).findByCode("NOPE");
        verify(spotGateway, times(1)).update(spot);
        verify(orderGateway, times(0)).create(any());
    }

    @Test
    @DisplayName("Given exhausted coupon, when execute, then returns error without order")
    void givenExhaustedCoupon_whenExecute_thenReturnsErrorWithoutOrder() {
        final var customer = givenCustomer();
        final var section = givenSection(Money.create(new BigDecimal("50.00"), BRL));
        final var spot = givenFreeSpot(section, "A1");
        final var coupon =
                Coupon.create("ONEUSE", null, section.getId().getValue(), CouponKind.PERCENT, 10, null, null, null, 1);
        when(couponGateway.findByCode("ONEUSE")).thenReturn(Optional.of(coupon));
        when(couponGateway.claimUse("ONEUSE", CLOCK.instant())).thenReturn(Optional.empty());

        final var notification = couponUseCase
                .execute(CreateOrderCommand.with(
                        customer.getId().getValue(), List.of(spot.getId().getValue()), null, "ONEUSE"))
                .getLeft();

        assertEquals(
                "Coupon is exhausted or expired: ONEUSE",
                notification.firstError().message());
        verify(customerGateway, times(1)).findById(customer.getId());
        verify(spotGateway, times(1)).findPlacement(spot.getId());
        verify(spotGateway, times(1)).reserveIfAvailable(spot.getId());
        verify(sectionGateway, times(1)).findById(section.getId());
        verify(couponGateway, times(1)).findByCode("ONEUSE");
        verify(couponGateway, times(1)).claimUse("ONEUSE", CLOCK.instant());
        verify(spotGateway, times(1)).update(spot);
        verify(orderGateway, times(0)).create(any());
    }

    @Test
    @DisplayName("Given coupon for other section, when execute, then returns error without order")
    void givenCouponForOtherSection_whenExecute_thenReturnsErrorWithoutOrder() {
        final var customer = givenCustomer();
        final var section = givenSection(Money.create(new BigDecimal("50.00"), BRL));
        final var spot = givenFreeSpot(section, "A1");
        final var coupon =
                Coupon.create("OTHER", null, "other-section", CouponKind.PERCENT, 10, null, null, null, null);
        when(couponGateway.findByCode("OTHER")).thenReturn(Optional.of(coupon));

        final var notification = couponUseCase
                .execute(CreateOrderCommand.with(
                        customer.getId().getValue(), List.of(spot.getId().getValue()), null, "OTHER"))
                .getLeft();

        assertEquals(
                "Coupon does not apply to these spots: OTHER",
                notification.firstError().message());
        verify(customerGateway, times(1)).findById(customer.getId());
        verify(spotGateway, times(1)).findPlacement(spot.getId());
        verify(spotGateway, times(1)).reserveIfAvailable(spot.getId());
        verify(sectionGateway, times(1)).findById(section.getId());
        verify(couponGateway, times(1)).findByCode("OTHER");
        verify(spotGateway, times(1)).update(spot);
        verify(orderGateway, times(0)).create(any());
    }
}
