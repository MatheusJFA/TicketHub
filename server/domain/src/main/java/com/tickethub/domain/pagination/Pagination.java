package com.tickethub.domain.pagination;

import static java.util.Objects.isNull;
import java.util.List;
import java.util.function.Function;

import com.tickethub.domain.exception.DomainException;

public record Pagination<T>(int currentPage, int perPage, long totalItems, List<T> items) {

    public Pagination {
        if (currentPage < 0) {
            throw new DomainException("'currentPage' must be >= 0");
        }
        if (perPage < 1) {
            throw new DomainException("'perPage' must be >= 1");
        }
        if (totalItems < 0) {
            throw new DomainException("'totalItems' must be >= 0");
        }
        
        items = isNull(items) ? List.of() : List.copyOf(items);
    }

    public <R> Pagination<R> map(Function<T, R> mapper) {
        final List<R> mappedItems = this.items.stream()
                .map(mapper)
                .toList();

        return new Pagination<>(this.currentPage, this.perPage, this.totalItems, mappedItems);
    }
}
