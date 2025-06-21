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

//Ajouter methode save pour creer event
    public Event save(Event event) {
        if (event.getId() == null) {
            entityManager.persist(event);
            return event;
        } else {
            return entityManager.merge(event);
        }
    }

    // Mettre à jour explicitement
    public Event update(Event event) {
        return entityManager.merge(event);
    }

    // Supprimer un événement
    public void delete(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }

    // Trouver un événement par ID
    public Optional<Event> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Event.class, id));
    }

    // Trouver tous les événements
    public List<Event> findAll() {
        return entityManager.createQuery(
                        "SELECT e FROM Event e ORDER BY e.eventDate ASC", Event.class)
                .getResultList();
    }

    // Compter le nombre total d'événements
    public long count() {
        return entityManager.createQuery("SELECT COUNT(e) FROM Event e", Long.class)
                .getSingleResult();
    }

    // Trouver les événements par statut
    public List<Event> findByStatus(EventStatus status) {
        return entityManager.createQuery(
                        "SELECT e FROM Event e WHERE e.status = :status ORDER BY e.eventDate ASC", Event.class)
                .setParameter("status", status)
                .getResultList();
    }

    // Trouver les événements par catégorie
    public List<Event> findByCategory(EventCategory category) {
        return entityManager.createQuery(
                        "SELECT e FROM Event e WHERE e.category = :category ORDER BY e.eventDate ASC", Event.class)
                .setParameter("category", category)
                .getResultList();
    }

    // Trouver les événements par ville
    public List<Event> findByCity(String city) {
        return entityManager.createQuery(
                        "SELECT e FROM Event e WHERE LOWER(e.city) LIKE LOWER(:city) ORDER BY e.eventDate ASC", Event.class)
                .setParameter("city", "%" + city + "%")
                .getResultList();
    }

    // Trouver les événements à venir
    public List<Event> findUpcomingEvents() {
        return entityManager.createQuery(
                        "SELECT e FROM Event e WHERE e.eventDate > :now AND e.status != :cancelled ORDER BY e.eventDate ASC", Event.class)
                .setParameter("now", LocalDateTime.now())
                .setParameter("cancelled", EventStatus.CANCELLED)
                .getResultList();
    }

    // Trouver les événements entre deux dates
    public List<Event> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return entityManager.createQuery(
                        "SELECT e FROM Event e WHERE e.eventDate BETWEEN :startDate AND :endDate ORDER BY e.eventDate ASC", Event.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    // Rechercher les événements par titre (mot-clé)
    public List<Event> searchByTitle(String title) {
        return entityManager.createQuery(
                        "SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(:title) ORDER BY e.eventDate ASC", Event.class)
                .setParameter("title", "%" + title + "%")
                .getResultList();
    }

    // Compter les événements par statut
    public long countByStatus(EventStatus status) {
        return entityManager.createQuery(
                        "SELECT COUNT(e) FROM Event e WHERE e.status = :status", Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    // Obtenir les événements les plus populaires (tickets vendus)
    public List<Event> findMostPopularEvents(int limit) {
        return entityManager.createQuery(
                        "SELECT e FROM Event e ORDER BY e.ticketsSold DESC", Event.class)
                .setMaxResults(limit)
                .getResultList();
    }

    // Vérifier si un événement existe par ID
    public boolean existsById(Long id) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(e) FROM Event e WHERE e.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return count > 0;
    }
}
