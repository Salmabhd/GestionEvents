package com.example.projetsdr.repository;


import com.example.projetsdr.model.Event;
import com.example.projetsdr.model.Event.EventCategory;
import com.example.projetsdr.model.Event.EventStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
public class EventRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // Méthodes CRUD de base
    public Event save(Event event) {
        if (event.getId() == null) {
            entityManager.persist(event);
            return event;
        } else {
            return entityManager.merge(event);
        }
    }

    public Optional<Event> findById(Long id) {
        Event event = entityManager.find(Event.class, id);
        return Optional.ofNullable(event);
    }

    public List<Event> findAll() {
        TypedQuery<Event> query = entityManager.createQuery(
                "SELECT e FROM Event e ORDER BY e.eventDate DESC", Event.class);
        return query.getResultList();
    }

    public void delete(Event event) {
        if (entityManager.contains(event)) {
            entityManager.remove(event);
        } else {
            entityManager.remove(entityManager.merge(event));
        }
    }

    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }

    // Méthodes de recherche avancée
    public List<Event> findByCategory(EventCategory category) {
        TypedQuery<Event> query = entityManager.createQuery(
                "SELECT e FROM Event e WHERE e.category = :category ORDER BY e.eventDate DESC",
                Event.class);
        query.setParameter("category", category);
        return query.getResultList();
    }

    public List<Event> findByStatus(EventStatus status) {
        TypedQuery<Event> query = entityManager.createQuery(
                "SELECT e FROM Event e WHERE e.status = :status ORDER BY e.eventDate DESC",
                Event.class);
        query.setParameter("status", status);
        return query.getResultList();
    }

    public List<Event> findByCategoryAndStatus(EventCategory category, EventStatus status) {
        TypedQuery<Event> query = entityManager.createQuery(
                "SELECT e FROM Event e WHERE e.category = :category AND e.status = :status ORDER BY e.eventDate DESC",
                Event.class);
        query.setParameter("category", category);
        query.setParameter("status", status);
        return query.getResultList();
    }

    public List<Event> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        TypedQuery<Event> query = entityManager.createQuery(
                "SELECT e FROM Event e WHERE e.eventDate BETWEEN :startDate AND :endDate ORDER BY e.eventDate ASC",
                Event.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }

    public List<Event> findByCity(String city) {
        TypedQuery<Event> query = entityManager.createQuery(
                "SELECT e FROM Event e WHERE LOWER(e.city) LIKE LOWER(:city) ORDER BY e.eventDate DESC",
                Event.class);
        query.setParameter("city", "%" + city + "%");
        return query.getResultList();
    }

    public List<Event> searchByTitle(String title) {
        TypedQuery<Event> query = entityManager.createQuery(
                "SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(:title) ORDER BY e.eventDate DESC",
                Event.class);
        query.setParameter("title", "%" + title + "%");
        return query.getResultList();
    }

    // Méthodes de filtrage combiné
    public List<Event> findWithFilters(EventCategory category, EventStatus status,
                                       String dateFilter, String searchTerm) {
        StringBuilder jpql = new StringBuilder("SELECT e FROM Event e WHERE 1=1");

        if (category != null) {
            jpql.append(" AND e.category = :category");
        }

        if (status != null) {
            jpql.append(" AND e.status = :status");
        }

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            jpql.append(" AND (LOWER(e.title) LIKE LOWER(:searchTerm) OR LOWER(e.description) LIKE LOWER(:searchTerm))");
        }

        // Filtre par date
        LocalDateTime now = LocalDateTime.now();
        if ("thisWeek".equals(dateFilter)) {
            jpql.append(" AND e.eventDate BETWEEN :startDate AND :endDate");
        } else if ("thisMonth".equals(dateFilter)) {
            jpql.append(" AND e.eventDate BETWEEN :startDate AND :endDate");
        } else if ("next3Months".equals(dateFilter)) {
            jpql.append(" AND e.eventDate BETWEEN :startDate AND :endDate");
        }

        jpql.append(" ORDER BY e.eventDate DESC");

        TypedQuery<Event> query = entityManager.createQuery(jpql.toString(), Event.class);

        if (category != null) {
            query.setParameter("category", category);
        }

        if (status != null) {
            query.setParameter("status", status);
        }

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            query.setParameter("searchTerm", "%" + searchTerm + "%");
        }

        // Paramètres de date
        if ("thisWeek".equals(dateFilter)) {
            LocalDateTime startOfWeek = now.withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfWeek = startOfWeek.plusDays(7);
            query.setParameter("startDate", startOfWeek);
            query.setParameter("endDate", endOfWeek);
        } else if ("thisMonth".equals(dateFilter)) {
            LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
            query.setParameter("startDate", startOfMonth);
            query.setParameter("endDate", endOfMonth);
        } else if ("next3Months".equals(dateFilter)) {
            LocalDateTime start = now.withHour(0).withMinute(0).withSecond(0);
            LocalDateTime end = start.plusMonths(3);
            query.setParameter("startDate", start);
            query.setParameter("endDate", end);
        }

        return query.getResultList();
    }

    // Méthodes de statistiques
    public Long countAll() {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(e) FROM Event e", Long.class);
        return query.getSingleResult();
    }

    public Long countByStatus(EventStatus status) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(e) FROM Event e WHERE e.status = :status", Long.class);
        query.setParameter("status", status);
        return query.getSingleResult();
    }

    public Long countByCategory(EventCategory category) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(e) FROM Event e WHERE e.category = :category", Long.class);
        query.setParameter("category", category);
        return query.getSingleResult();
    }

    // Méthodes pour les événements récents
    public List<Event> findUpcomingEvents(int limit) {
        TypedQuery<Event> query = entityManager.createQuery(
                "SELECT e FROM Event e WHERE e.eventDate > :now AND e.status = :status ORDER BY e.eventDate ASC",
                Event.class);
        query.setParameter("now", LocalDateTime.now());
        query.setParameter("status", EventStatus.UPCOMING);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    public List<Event> findActiveEvents() {
        TypedQuery<Event> query = entityManager.createQuery(
                "SELECT e FROM Event e WHERE e.status = :status ORDER BY e.eventDate DESC",
                Event.class);
        query.setParameter("status", EventStatus.ACTIVE);
        return query.getResultList();
    }
}