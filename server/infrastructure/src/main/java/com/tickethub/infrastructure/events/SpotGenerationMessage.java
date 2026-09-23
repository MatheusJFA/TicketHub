package com.tickethub.infrastructure.events;

public record SpotGenerationMessage(String type, String showId, String sectionId, String sectionCode, long totalSpots) {

    public static final String TYPE = "SpotsGenerationRequested";
}
