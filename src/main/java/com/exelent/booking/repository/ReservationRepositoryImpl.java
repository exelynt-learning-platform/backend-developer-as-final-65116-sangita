package com.exelent.booking.repository;

import com.exelent.booking.domain.Reservation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public class ReservationRepositoryImpl implements ReservationRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Reservation> search(Specification<Reservation> spec, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        return new PageImpl<>(loadPage(spec, pageable, cb), pageable, count(spec, cb));
    }

    private long count(Specification<Reservation> spec, CriteriaBuilder cb) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Reservation> root = countQuery.from(Reservation.class);
        countQuery.select(cb.countDistinct(root));
        Predicate predicate = spec.toPredicate(root, countQuery, cb);
        if (predicate != null) {
            countQuery.where(predicate);
        }
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<Reservation> loadPage(Specification<Reservation> spec, Pageable pageable, CriteriaBuilder cb) {
        CriteriaQuery<Reservation> dataQuery = cb.createQuery(Reservation.class);
        Root<Reservation> root = dataQuery.from(Reservation.class);
        dataQuery.select(root);
        Predicate predicate = spec.toPredicate(root, dataQuery, cb);
        if (predicate != null) {
            dataQuery.where(predicate);
        }
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    orders.add(cb.asc(root.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(root.get(order.getProperty())));
                }
            });
            dataQuery.orderBy(orders);
        }

        TypedQuery<Reservation> query = entityManager.createQuery(dataQuery);
        query.setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("Reservation.withUserAndResource"));
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        return query.getResultList();
    }
}
