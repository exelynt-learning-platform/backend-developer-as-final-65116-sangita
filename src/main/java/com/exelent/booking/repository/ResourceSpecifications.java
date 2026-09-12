package com.exelent.booking.repository;

import com.exelent.booking.domain.BookableResource;
import com.exelent.booking.domain.ResourceType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class ResourceSpecifications {

    private ResourceSpecifications() {
    }

    private static String likeContains(String name) {
        String escaped = name.trim()
                .toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    public static Specification<BookableResource> withFilters(ResourceType type, Boolean available, String name) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (available != null) {
                predicates.add(cb.equal(root.get("available"), available));
            }
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), likeContains(name), '\\'));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
