package com.tickethub.application.show.reschedule;

import java.time.OffsetDateTime;

public record RescheduleShowCommand(String id, OffsetDateTime date) {
    public static RescheduleShowCommand with(final String id, final OffsetDateTime date) {
        return new RescheduleShowCommand(id, date);
    }
}
