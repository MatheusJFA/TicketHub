package com.tickethub.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.validation.Notification;

class DomainValidatorsTest {

    @Test
    void givenValidCustomer_whenValidate_thenHaveNoErrors() {
        final var notification = Notification.create();
        Customer.create("12345678909", "John Doe").validate(notification);
        assertFalse(notification.hasError());
    }

    @Test
    void givenValidPartner_whenValidate_thenHaveNoErrors() {
        final var notification = Notification.create();
        Partner.create("Cinema Nova", "11222333000181").validate(notification);
        assertFalse(notification.hasError());
    }

    @Test
    void givenSpotWithoutLocation_whenValidate_thenReportError() {
        final var notification = Notification.create();
        Spot.create(null).validate(notification);
        assertEquals("'location' should not be null", notification.firstError().message());
    }

    @Test
    void givenSectionWithoutPrice_whenValidate_thenReportError() {
        final var notification = Notification.create();
        Section.create("VIP", "Description", 10, null, new HashSet<>()).validate(notification);
        assertEquals("'price' should not be null", notification.firstError().message());
    }

    @Test
    void givenShowWithoutPartner_whenValidate_thenReportError() {
        final var notification = Notification.create();
        Show.create("Concert", "Description", 10, null, new HashSet<>()).validate(notification);
        assertEquals("'partnerId' should not be null", notification.firstError().message());
    }

}
