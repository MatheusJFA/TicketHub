package com.tickethub.domain.core.section;

import java.util.Optional;

import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;

public interface SectionGateway {
    Section create(Section section);
    void deleteById(SectionID id);
    Optional<Section> findById(SectionID id);
    Section update(Section section);
    Pagination<Section> findAll(SearchQuery query);
}