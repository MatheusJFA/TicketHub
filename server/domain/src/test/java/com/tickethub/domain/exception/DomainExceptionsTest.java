package com.tickethub.domain.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Domain exceptions")
class DomainExceptionsTest {

    @Test
    @DisplayName("Given resource and id, when create not found, then message identifies both")
    void givenResourceAndId_whenCreateNotFound_thenMessageIdentifiesBoth() {
        final var exception = new ResourceNotFoundException("Spot", "spot-1");

        assertInstanceOf(DomainException.class, exception);
        assertEquals("Spot not found: spot-1", exception.getMessage());
    }

    @Test
    @DisplayName("Given reused spot, when create already used, then message is fixed")
    void givenReusedSpot_whenCreateAlreadyUsed_thenMessageIsFixed() {
        final var exception = new SpotAlreadyUsedException();

        assertInstanceOf(DomainException.class, exception);
        assertEquals("Spot is already used", exception.getMessage());
    }

    @Test
    @DisplayName("Given foreign ticket, when create ownership error, then message is fixed")
    void givenForeignTicket_whenCreateOwnershipError_thenMessageIsFixed() {
        final var exception = new SpotOwnershipException();

        assertInstanceOf(DomainException.class, exception);
        assertEquals("Spot does not belong to the given show and section", exception.getMessage());
    }

    @Test
    @DisplayName("Given show date, when create outside date error, then message carries the date")
    void givenShowDate_whenCreateOutsideDateError_thenMessageCarriesTheDate() {
        final var showDate = OffsetDateTime.parse("2027-01-14T20:00:00-03:00");

        final var exception = new ShowOutsideCheckInDateException(showDate);

        assertInstanceOf(DomainException.class, exception);
        assertEquals("Show is outside the check-in date (showDate=2027-01-14T20:00-03:00)", exception.getMessage());
    }
}
