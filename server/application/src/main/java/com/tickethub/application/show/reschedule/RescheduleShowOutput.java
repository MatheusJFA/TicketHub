package com.tickethub.application.show.reschedule;

import com.tickethub.domain.core.show.Show;

public record RescheduleShowOutput(String id) {
    public static RescheduleShowOutput from(final String id) {
        return new RescheduleShowOutput(id);
    }

    public static RescheduleShowOutput from(final Show entity) {
        return from(entity.getId().getValue());
    }
}
