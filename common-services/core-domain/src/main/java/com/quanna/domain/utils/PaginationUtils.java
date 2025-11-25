package com.quanna.domain.utils;

import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.List;

import com.quanna.domain.response.PageResponse;
import org.springframework.data.domain.Page;

/**
 * Utility class for pagination operations
 */
public final class PaginationUtils {
    private PaginationUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Convert Spring Data Page to custom PageResponse
     */
    public static <T> PageResponse<T> toPageResponse(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    /**
     * Convert Spring Data Page to custom PageResponse with mapping function
     */
    public static <T, R> PageResponse<R> toPageResponse(Page<T> page, Function<T, R> mapper) {
        List<R> mappedContent = page.getContent().stream()
                .map(mapper)
                .collect(Collectors.toList());
        return PageResponse.<R>builder()
                .content(mappedContent)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }
}
