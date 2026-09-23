package com.tickethub.application.spot.retrieve.bysection;

import com.tickethub.domain.pagination.SearchQuery;

public record ListSectionSpotsCommand(String sectionId, SearchQuery query) {
    public static ListSectionSpotsCommand with(final String sectionId, final SearchQuery query) {
        return new ListSectionSpotsCommand(sectionId, query);
    }
}
