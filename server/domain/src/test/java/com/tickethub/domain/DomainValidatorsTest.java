package com.tickethub.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.validation.Notification;

@DisplayName("Domain validators")
class DomainValidatorsTest {

    @Test
    @DisplayName("Given valid customer, when validate, then have no errors")
    void givenValidCustomer_whenValidate_thenHaveNoErrors() {
        final var notification = Notification.create();
        Customer.create("12345678909", "John Doe", "john@domain.com",
                "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS").validate(notification);
        assertFalse(notification.hasError());
    }

    @Test
    @DisplayName("Given valid partner, when validate, then have no errors")
    void givenValidPartner_whenValidate_thenHaveNoErrors() {
        final var notification = Notification.create();
        Partner.create("Cinema Nova", "11222333000181", com.tickethub.domain.shared.Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"), "cinema@domain.com",
                "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS").validate(notification);
        assertFalse(notification.hasError());
    }

    @Test
    @DisplayName("Given spot without location, when validate, then have no errors")
    void givenSpotWithoutLocation_whenValidate_thenHaveNoErrors() {
        final var notification = Notification.create();
        Spot.create(null).validate(notification);
        assertFalse(notification.hasError());
    }

    @Test
    @DisplayName("Given section without price, when validate, then report error")
    void givenSectionWithoutPrice_whenValidate_thenReportError() {
        final var notification = Notification.create();
        Section.create("VIP", "Description", 10, null).validate(notification);
        assertEquals("'price' should not be null", notification.firstError().message());
    }

    @Test
    @DisplayName("Given show without partner, when validate, then report error")
    void givenShowWithoutPartner_whenValidate_thenReportError() {
        final var notification = Notification.create();
        Show.create("Concert", "Description", OffsetDateTime.parse("2027-01-15T20:00:00-03:00"), Address.create("Rua Augusta", "100", null, "Centro", "São Paulo", "SP", "Brasil", "01305-000"), 10, null)
                .validate(notification);
        assertEquals("'partnerId' should not be null", notification.firstError().message());
    }

}
