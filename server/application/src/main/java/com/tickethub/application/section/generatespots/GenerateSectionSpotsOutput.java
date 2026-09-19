package com.tickethub.application.section.generatespots;

public record GenerateSectionSpotsOutput(String sectionId, long generatedSpots) {
    public static GenerateSectionSpotsOutput from(final String sectionId, final long generatedSpots) {
        return new GenerateSectionSpotsOutput(sectionId, generatedSpots);
    }
}
