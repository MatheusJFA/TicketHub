package com.tickethub.domain.core.section;

import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import java.util.List;
import java.util.Optional;

public interface SectionGateway {
    Section create(Section section, ShowID showId);

    void deleteById(SectionID id);

    Optional<Section> findById(SectionID id);

    Section update(Section section);

    Pagination<Section> findAll(SearchQuery query);

    Pagination<Section> findByShowId(ShowID showId, SearchQuery query);

    List<SectionID> existsByIds(List<SectionID> ids);
}
