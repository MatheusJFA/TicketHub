package com.tickethub.infrastructure.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tickethub.application.Either;
import com.tickethub.application.coupon.create.CreateCouponOutput;
import com.tickethub.application.coupon.create.CreateCouponUseCase;
import com.tickethub.infrastructure.ControllerTest;
import com.tickethub.infrastructure.coupon.presenters.CouponMapperImpl;
import com.tickethub.infrastructure.security.TestTokens;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ControllerTest(controllers = CouponController.class)
@Import({CouponMapperImpl.class})
@DisplayName("Coupon controller")
class CouponControllerTest {
    @Autowired
    MockMvc mvc;

    @MockitoBean
    CreateCouponUseCase createCoupon;

    @Value("${tickethub.security.jwt.secret}")
    String jwtSecret;

    private String bearer(final String ownerId, final String... authorities) {
        return "Bearer " + TestTokens.bearer(jwtSecret, ownerId, authorities);
    }

    @Test
    @DisplayName("Given admin, when calls create coupon, should return coupon id")
    void givenAdmin_whenCallsCreateCoupon_shouldReturnCouponId() throws Exception {
        when(createCoupon.execute(any())).thenReturn(Either.right(new CreateCouponOutput("coupon-1", "PISTA10")));

        mvc.perform(post("/coupons")
                        .header("Authorization", bearer(null, "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"code":"PISTA10","sectionId":"section-1","kind":"PERCENT","percent":10}
                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/coupons/coupon-1"))
                .andExpect(jsonPath("$.id").value("coupon-1"));
    }

    @Test
    @DisplayName("Given anonymous, when calls create coupon, then returns unauthorized")
    void givenAnonymous_whenCallsCreateCoupon_thenReturnsUnauthorized() throws Exception {
        mvc.perform(post("/coupons").contentType(MediaType.APPLICATION_JSON).content("""
                {"code":"PISTA10","sectionId":"section-1","kind":"PERCENT","percent":10}
                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Given non admin, when calls create coupon, then returns forbidden")
    void givenNonAdmin_whenCallsCreateCoupon_thenReturnsForbidden() throws Exception {
        mvc.perform(post("/coupons")
                        .header("Authorization", bearer("partner-1", "ROLE_PARTNER", "show:create"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"code":"PISTA10","sectionId":"section-1","kind":"PERCENT","percent":10}
                """))
                .andExpect(status().isForbidden());
    }
}
