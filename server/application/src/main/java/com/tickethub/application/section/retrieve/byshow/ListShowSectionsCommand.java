package com.tickethub.application.section.retrieve.byshow;

import com.tickethub.domain.pagination.SearchQuery;

public record ListShowSectionsCommand(String showId, SearchQuery query) {
    public static ListShowSectionsCommand with(final String showId, final SearchQuery query) {
        return new ListShowSectionsCommand(showId, query);
    }
}
