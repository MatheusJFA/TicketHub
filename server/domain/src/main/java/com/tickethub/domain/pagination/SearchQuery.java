package com.tickethub.domain.pagination;

public record SearchQuery(
    int page,
    int perPage,
    String searchTerm,
    String sort,
    String direction
) {
}
