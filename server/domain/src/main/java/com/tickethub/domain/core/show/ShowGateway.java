package com.tickethub.domain.core.show;

import java.util.Optional;
import java.util.Set;

import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;

public interface ShowGateway {
    Show create(Show show);
    void deleteById(ShowID id);
    Optional<Show> findById(ShowID id);
    Show update(Show show);
    Pagination<Show> findAll(SearchQuery query);

    /**
     * Bulk-inserts new spots for a section of the show, linking them to the
     * section. Used by asynchronous spot generation to avoid rewriting the
     * whole aggregate graph; a no-op when {@code spots} is empty.
     */
    void appendSpots(ShowID showId, SectionID sectionId, Set<Spot> spots);
}