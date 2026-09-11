package com.exelent.booking.dto;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;

public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last,
        String sort
) {
    public static <T> PagedResponse<T> from(Page<T> page) {
        String sort = page.getSort().stream()
                .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                .collect(Collectors.joining(";"));
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast(),
                sort.isBlank() ? null : sort
        );
    }
}
