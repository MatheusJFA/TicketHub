package com.tickethub.domain.core.spot;

import java.util.List;
import java.util.Optional;

import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;

public interface SpotGateway {
    Spot create(Spot spot, SectionID sectionId);
    void deleteById(SpotID id);
    Optional<Spot> findById(SpotID id);
    /**
     * Atomically reserves a free spot for an open order. Returns the reserved
     * spot, or empty when it is already reserved, used or missing, so
     * concurrent buyers cannot hold the same seat.
     */
    Optional<Spot> reserveIfAvailable(SpotID id);
    /**
     * Loads the spot together with the show/section it belongs to, for
     * ticket (QR code) validation at the door.
     */
    Optional<SpotPlacement> findPlacement(SpotID id);
    Spot update(Spot spot);
    Pagination<Spot> findAll(SearchQuery query);
    Pagination<Spot> findBySection(SectionID sectionId, SearchQuery query);
    List<SpotID> existsByIds(List<SpotID> ids);
}
