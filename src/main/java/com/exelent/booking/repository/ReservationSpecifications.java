package com.exelent.booking.repository;

import com.exelent.booking.domain.Reservation;
import com.exelent.booking.domain.ReservationStatus;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class ReservationSpecifications {

    private ReservationSpecifications() {
    }

    public static Specification<Reservation> withFilters(
            Long userId,
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        return (root, query, cb) -> {
            if (!isCountQuery(query)) {
                root.fetch("user", JoinType.INNER);
                root.fetch("resource", JoinType.INNER);
            }
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static boolean isCountQuery(CriteriaQuery<?> query) {
        if (query == null || query.getResultType() == null) {
            return false;
        }
        Class<?> resultType = query.getResultType();
        return resultType == Long.class || resultType == long.class;
    }
}
