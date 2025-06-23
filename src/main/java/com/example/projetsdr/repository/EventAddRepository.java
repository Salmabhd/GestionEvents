package com.example.projetsdr.repository;

import com.example.projetsdr.model.Event;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
@Transactional
public class EventAddRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Event save(Event event) {
        Event savedEvent;

        if (event.getId() == null) {
            // Nouvelle entité - utiliser persist
            entityManager.persist(event);
            savedEvent = event;
        } else {
            // Entité existante - utiliser merge
            savedEvent = entityManager.merge(event);
        }

        // Force l'écriture immédiate en base
        entityManager.flush();

        return savedEvent;
    }

    public Event findById(Long id) {
        return entityManager.find(Event.class, id);
    }

    public List<Event> findAll() {
        // Vide le cache pour récupérer les données fraîches
        entityManager.clear();
        return entityManager
                .createQuery("SELECT e FROM Event e ORDER BY e.eventDate ASC", Event.class)
                .getResultList();
    }

    public Event update(Event event) {
        Event merged = entityManager.merge(event);
        entityManager.flush();
        return merged;
    }

    public void delete(Long id) {
        Event event = findById(id);
        if (event != null) {
            entityManager.remove(event);
            entityManager.flush();
        }
    }

    public long count() {
        entityManager.clear();
        return entityManager
                .createQuery("SELECT COUNT(e) FROM Event e", Long.class)
                .getSingleResult();
    }
}