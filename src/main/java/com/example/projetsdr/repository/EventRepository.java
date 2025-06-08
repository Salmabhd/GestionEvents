package com.example.projetsdr.repository;


import com.example.projetsdr.model.Event;
import com.example.projetsdr.model.Event.EventCategory;
import com.example.projetsdr.model.Event.EventStatus;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class EventRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // Créer un nouvel événement
    public Event save(Event event) {
        if (event.getId() == null) {
            entityManager.persist(event);
            return event;
        } else {
            return entityManager.merge(event);
        }
    }

    // Trouver un événement par ID
    public Optional<Event> findById(Long id) {
        Event event = entityManager.find(Event.class, id);
        return Optional.ofNullable(event);
    }

    // Trouver tous les événements
    public List<Event> findAll() {
        TypedQuery<Event> query = entityManager.createQuery(
                "SELECT e FROM Event e ORDER BY e.eventDate ASC", Event.class);
        return query.getResultList();
    }

    // Trouver les événements par statut
    public List<Event> findByStatus(Event.EventStatus status) {
        TypedQuery<Event> query = entityManager.createQuery(
                "SELECT e FROM Event e WHERE e.status = :status ORDER BY e.eventDate ASC", Event.class);
        query.setParameter("status", status);
        return query.getResultList();
    }

    // Trouver les événements par catégorie
    public List<Event> findByCategory(Event.EventCategory category) {
        TypedQuery<Event> query = entityManager.createQuery(
                "SELECT e FROM Event e WHERE e.category = :category ORDER BY e.eventDate ASC", Event.class);
        query.setParameter("category", category);
        return query.getResultList();
    }

    // Trouver les événements par ville
    public List<Event> findByCity(String city) {
        TypedQuery<Event> query = entityManager.createQuery(
                "SELECT e FROM Event e WHERE LOWER(e.city) LIKE LOWER(:city) ORDER BY e.eventDate ASC", Event.class);
        query.setParameter("city", "%" + city + "%");
        return query.getResultList();
    }

    // Trouver les événements à venir
    public List<Event> findUpcomingEvents() {
        TypedQuery<Event> query = entityManager.createQuery(
                "SELECT e FROM Event e WHERE e.eventDate > :now AND e.status != :cancelled ORDER BY e.eventDate ASC", Event.class);
        query.setParameter("now", LocalDateTime.now());
        query.setParameter("cancelled", Event.EventStatus.CANCELLED);
        return query.getResultList();
    }

    // Trouver les événements par plage de dates
    public List<Event> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        TypedQuery<Event> query = entityManager.createQuery(
                "SELECT e FROM Event e WHERE e.eventDate BETWEEN :startDate AND :endDate ORDER BY e.eventDate ASC", Event.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }

    // Rechercher des événements par titre
    public List<Event> searchByTitle(String title) {
        TypedQuery<Event> query = entityManager.createQuery(
                "SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(:title) ORDER BY e.eventDate ASC", Event.class);
        query.setParameter("title", "%" + title + "%");
        return query.getResultList();
    }

    // Supprimer un événement
    public void delete(Long id) {
        Event event = entityManager.find(Event.class, id);
        if (event != null) {
            entityManager.remove(event);
        }
    }

    // Compter le nombre total d'événements
    public long count() {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(e) FROM Event e", Long.class);
        return query.getSingleResult();
    }

    // Compter les événements par statut
    public long countByStatus(Event.EventStatus status) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(e) FROM Event e WHERE e.status = :status", Long.class);
        query.setParameter("status", status);
        return query.getSingleResult();
    }

    // Trouver les événements les plus populaires (par tickets vendus)
    public List<Event> findMostPopularEvents(int limit) {
        TypedQuery<Event> query = entityManager.createQuery(
                "SELECT e FROM Event e ORDER BY e.ticketsSold DESC", Event.class);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    // Vérifier si un événement existe
    public boolean existsById(Long id) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(e) FROM Event e WHERE e.id = :id", Long.class);
        query.setParameter("id", id);
        return query.getSingleResult() > 0;
    }
}