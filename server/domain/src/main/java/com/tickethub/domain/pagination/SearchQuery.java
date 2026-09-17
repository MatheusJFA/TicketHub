package com.tickethub.domain.pagination;

import com.tickethub.domain.exception.DomainException;

public record SearchQuery(
    int page,
    int perPage,
    String searchTerm,
    String sort,
    String direction
) {
    public SearchQuery {
        if (page < 0) {
            throw new DomainException("'page' must be >= 0");
        }
        if (perPage < 1 || perPage > 100) {
            throw new DomainException("'perPage' must be between 1 and 100");
        }
    }
}
