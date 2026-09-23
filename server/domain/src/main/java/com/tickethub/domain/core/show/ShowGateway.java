package com.tickethub.domain.core.show;

import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ShowGateway {
    Show create(Show show);

    void deleteById(ShowID id);

    Optional<Show> findById(ShowID id);

    Show update(Show show);

    Pagination<Show> findAll(SearchQuery query);

    List<ShowID> existsByIds(List<ShowID> ids);

    /**
     * Bulk-inserts new spots for a section of the show, linking them to the
     * section. Used by asynchronous spot generation to avoid rewriting the
     * whole aggregate graph; a no-op when {@code spots} is empty.
     */
    void appendSpots(ShowID showId, SectionID sectionId, Set<Spot> spots);
}
