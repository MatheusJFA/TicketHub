package com.tickethub.application.coupon;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tickethub.application.UseCaseTest;
import com.tickethub.application.coupon.create.CreateCouponCommand;
import com.tickethub.application.coupon.create.DefaultCreateCouponUseCase;
import com.tickethub.domain.core.coupon.CouponGateway;
import com.tickethub.domain.core.coupon.CouponKind;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Create coupon use case")
class CreateCouponUseCaseTest extends UseCaseTest {

    private final CouponGateway gateway = mock(CouponGateway.class);
    private final DefaultCreateCouponUseCase useCase = new DefaultCreateCouponUseCase(gateway);

    @Override
    protected List<Object> getMocks() {
        return List.of(gateway);
    }

    @Test
    @DisplayName("Creates percent coupon")
    void createsPercentCoupon() {
        when(gateway.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final var output = useCase.execute(CreateCouponCommand.with(
                        "pista10", null, "section-1", CouponKind.PERCENT, 10, null, null, null, null, null))
                .getRight();

        assertEquals("PISTA10", output.code());
        assertNotNull(output.id());
        verify(gateway, times(1)).create(any());
    }

    @Test
    @DisplayName("Creates fixed coupon")
    void createsFixedCoupon() {
        when(gateway.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final var output = useCase.execute(CreateCouponCommand.with(
                        "FIX30",
                        "show-1",
                        null,
                        CouponKind.FIXED,
                        null,
                        new BigDecimal("30.00"),
                        "BRL",
                        null,
                        null,
                        100))
                .getRight();

        assertEquals("FIX30", output.code());
        verify(gateway, times(1)).create(any());
    }

    @Test
    @DisplayName("Creates coupon without scope as notification")
    void createsCouponWithoutScopeAsNotification() {
        final var notification = useCase.execute(CreateCouponCommand.with(
                        "NOSCOPE", null, null, CouponKind.PERCENT, 10, null, null, null, null, null))
                .getLeft();

        assertEquals(
                "coupon must target either a show or a section",
                notification.firstError().message());
        verify(gateway, never()).create(any());
    }

    @Test
    @DisplayName("Creates percent coupon out of range as notification")
    void createsPercentCouponOutOfRangeAsNotification() {
        final var notification = useCase.execute(CreateCouponCommand.with(
                        "BADPCT", null, "section-1", CouponKind.PERCENT, 0, null, null, null, null, null))
                .getLeft();

        assertEquals(
                "'percent' should be between 1 and 100",
                notification.firstError().message());
        verify(gateway, never()).create(any());
    }
}
