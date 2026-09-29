package com.company.andy.common.utils;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Pageable;

import java.util.List;

public record PagedResponse<T>(
        @Schema(description = "Paged elements") List<T> content,
        @Schema(description = "Total number of elements") long totalElements,
        @Schema(description = "Total number of pages") long totalPages,
        @Schema(description = "Page number") int pageNumber,
        @Schema(description = "Page size") int pageSize,
        @Schema(description = "Whether next page exists") boolean hasNext
) {
    public PagedResponse(List<T> content, Pageable pageable, long totalElements) {
        long totalPages = (long) Math.ceil((double) totalElements / pageable.getPageSize());
        this(
                content,
                totalElements,
                totalPages,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getPageNumber() + 1 < totalPages
        );
    }

    public static <T> PagedResponse<T> empty(Pageable pageable) {
        return new PagedResponse<>(List.of(), pageable, 0);
    }
}

