package com.tickethub.domain.exception;

import java.time.OffsetDateTime;

/**
 * A ticket was presented outside the show date, so check-in is refused.
 */
public class ShowOutsideCheckInDateException extends DomainException {

    public ShowOutsideCheckInDateException(final OffsetDateTime showDate) {
        super("Show is outside the check-in date (showDate=" + showDate + ")");
    }
}
