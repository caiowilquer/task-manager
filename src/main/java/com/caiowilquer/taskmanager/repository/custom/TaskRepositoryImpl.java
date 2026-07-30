package com.caiowilquer.taskmanager.repository.custom;

import com.caiowilquer.taskmanager.dto.task.SortDirection;
import com.caiowilquer.taskmanager.dto.task.TaskSortField;
import com.caiowilquer.taskmanager.entity.Task;
import com.caiowilquer.taskmanager.entity.enums.TaskPriority;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class TaskRepositoryImpl implements TaskQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Task> search(
            UUID projectId,
            TaskSearchCriteria criteria,
            Pageable pageable
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Task> query = criteriaBuilder.createQuery(Task.class);
        Root<Task> root = query.from(Task.class);

        root.fetch("project", JoinType.INNER);
        root.fetch("assignee", JoinType.INNER);
        root.fetch("createdBy", JoinType.INNER);

        query.select(root);
        query.where(
                predicates(criteriaBuilder, root, projectId, criteria)
                        .toArray(Predicate[]::new)
        );
        query.orderBy(orderBy(criteriaBuilder, root, criteria));

        TypedQuery<Task> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(Math.toIntExact(pageable.getOffset()));
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Task> tasks = typedQuery.getResultList();
        long total = count(projectId, criteria);

        return new PageImpl<>(tasks, pageable, total);
    }

    private long count(
            UUID projectId,
            TaskSearchCriteria criteria
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery =
                criteriaBuilder.createQuery(Long.class);

        Root<Task> root = countQuery.from(Task.class);

        countQuery.select(criteriaBuilder.count(root));
        countQuery.where(
                predicates(criteriaBuilder, root, projectId, criteria)
                        .toArray(Predicate[]::new)
        );

        return entityManager
                .createQuery(countQuery)
                .getSingleResult();
    }

    private List<Predicate> predicates(
            CriteriaBuilder criteriaBuilder,
            Root<Task> root,
            UUID projectId,
            TaskSearchCriteria criteria
    ) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(
                criteriaBuilder.equal(
                        root.get("project").get("id"),
                        projectId
                )
        );

        if (criteria.status() != null) {
            predicates.add(
                    criteriaBuilder.equal(
                            root.get("status"),
                            criteria.status()
                    )
            );
        }

        if (criteria.priority() != null) {
            predicates.add(
                    criteriaBuilder.equal(
                            root.get("priority"),
                            criteria.priority()
                    )
            );
        }

        if (criteria.assigneeId() != null) {
            predicates.add(
                    criteriaBuilder.equal(
                            root.get("assignee").get("id"),
                            criteria.assigneeId()
                    )
            );
        }

        if (criteria.createdFrom() != null) {
            predicates.add(
                    criteriaBuilder.greaterThanOrEqualTo(
                            root.get("createdAt"),
                            criteria.createdFrom()
                    )
            );
        }

        if (criteria.createdTo() != null) {
            predicates.add(
                    criteriaBuilder.lessThanOrEqualTo(
                            root.get("createdAt"),
                            criteria.createdTo()
                    )
            );
        }

        if (criteria.deadlineFrom() != null) {
            predicates.add(
                    criteriaBuilder.greaterThanOrEqualTo(
                            root.get("deadline"),
                            criteria.deadlineFrom()
                    )
            );
        }

        if (criteria.deadlineTo() != null) {
            predicates.add(
                    criteriaBuilder.lessThanOrEqualTo(
                            root.get("deadline"),
                            criteria.deadlineTo()
                    )
            );
        }

        if (criteria.query() != null && !criteria.query().isBlank()) {
            String normalizedQuery = criteria.query()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            String pattern =
                    "%" + escapeLike(normalizedQuery) + "%";

            Predicate titleContains = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    pattern,
                    '\\'
            );

            Predicate descriptionContains = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    pattern,
                    '\\'
            );

            predicates.add(
                    criteriaBuilder.or(
                            titleContains,
                            descriptionContains
                    )
            );
        }

        return predicates;
    }

    private List<Order> orderBy(
            CriteriaBuilder criteriaBuilder,
            Root<Task> root,
            TaskSearchCriteria criteria
    ) {
        TaskSortField sortField = criteria.sortBy() == null
                ? TaskSortField.CREATED_AT
                : criteria.sortBy();

        SortDirection direction = criteria.direction() == null
                ? SortDirection.DESC
                : criteria.direction();

        List<Order> orders = new ArrayList<>();

        Expression<?> sortExpression;

        if (sortField == TaskSortField.PRIORITY) {
            sortExpression = priorityRank(
                    criteriaBuilder,
                    root
            );
        } else if (sortField == TaskSortField.DEADLINE) {
            addDeadlineNullOrdering(
                    criteriaBuilder,
                    root,
                    orders
            );

            sortExpression = root.get("deadline");
        } else {
            sortExpression = root.get("createdAt");
        }

        Order requestedOrder = direction == SortDirection.ASC
                ? criteriaBuilder.asc(sortExpression)
                : criteriaBuilder.desc(sortExpression);

        orders.add(requestedOrder);

        // Stable ordering prevents records from moving unpredictably
        // between pages when the primary sort values are equal.
        orders.add(criteriaBuilder.asc(root.get("id")));

        return orders;
    }

    private Expression<Integer> priorityRank(
            CriteriaBuilder criteriaBuilder,
            Root<Task> root
    ) {
        return criteriaBuilder
                .<Integer>selectCase()
                .when(
                        criteriaBuilder.equal(
                                root.get("priority"),
                                TaskPriority.LOW
                        ),
                        1
                )
                .when(
                        criteriaBuilder.equal(
                                root.get("priority"),
                                TaskPriority.MEDIUM
                        ),
                        2
                )
                .when(
                        criteriaBuilder.equal(
                                root.get("priority"),
                                TaskPriority.HIGH
                        ),
                        3
                )
                .when(
                        criteriaBuilder.equal(
                                root.get("priority"),
                                TaskPriority.CRITICAL
                        ),
                        4
                )
                .otherwise(0);
    }

    private void addDeadlineNullOrdering(
            CriteriaBuilder criteriaBuilder,
            Root<Task> root,
            List<Order> orders
    ) {
        Expression<Integer> nullsLast = criteriaBuilder
                .<Integer>selectCase()
                .when(
                        criteriaBuilder.isNull(root.get("deadline")),
                        1
                )
                .otherwise(0);

        orders.add(criteriaBuilder.asc(nullsLast));
    }

    private String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}