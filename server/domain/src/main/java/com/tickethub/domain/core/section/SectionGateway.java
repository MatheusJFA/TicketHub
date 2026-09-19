package com.tickethub.domain.core.section;

import java.util.List;
import java.util.Optional;

import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;

public interface SectionGateway {
    Section create(Section section, ShowID showId);
    void deleteById(SectionID id);
    Optional<Section> findById(SectionID id);
    Section update(Section section);
    Pagination<Section> findAll(SearchQuery query);
    List<SectionID> existsByIds(List<SectionID> ids);
}