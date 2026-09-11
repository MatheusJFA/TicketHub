package com.tickethub.domain.pagination;

import java.util.List;
import java.util.function.Function;

public record Pagination<T>(int currentPage, int perPage, long totalItems, List<T> items) {

    public <R> Pagination<R> map(Function<T, R> mapper) {
        final List<R> mappedItems = this.items.stream()
                .map(mapper)
                .toList();

        return new Pagination<>(this.currentPage, this.perPage, this.totalItems, mappedItems);
    }
}
