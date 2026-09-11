package com.tickethub.domain.core.show;

import java.util.Optional;

import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;

public interface ShowGateway {
    Show create(Show show);
    void deleteById(ShowID id);
    Optional<Show> findById(ShowID id);
    Show update(Show show);
    Pagination<Show> findAll(SearchQuery query);
}