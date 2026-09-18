package com.tickethub.application.section.generatespots;

public record GenerateSectionSpotsCommand(String showId, String sectionId, String sectionCode) {
    public static GenerateSectionSpotsCommand with(final String showId, final String sectionId,
            final String sectionCode) {
        return new GenerateSectionSpotsCommand(showId, sectionId, sectionCode);
    }
}
