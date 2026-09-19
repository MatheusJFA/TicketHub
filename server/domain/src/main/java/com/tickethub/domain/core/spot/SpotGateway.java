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
    Spot update(Spot spot);
    Pagination<Spot> findAll(SearchQuery query);
    List<SpotID> existsByIds(List<SpotID> ids);
}
