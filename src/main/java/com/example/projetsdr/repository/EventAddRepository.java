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

    public void save(Event event) {
        entityManager.persist(event);
    }

    public Event findById(Long id) {
        return entityManager.find(Event.class, id);
    }

    public List<Event> findAll() {
        return entityManager
                .createQuery("SELECT e FROM Event e ORDER BY e.eventDate ASC", Event.class)
                .getResultList();
    }

    public void update(Event event) {
        entityManager.merge(event);
    }

    public void delete(Long id) {
        Event event = findById(id);
        if (event != null) {
            entityManager.remove(event);
        }
    }

    public long count() {
        return entityManager
                .createQuery("SELECT COUNT(e) FROM Event e", Long.class)
                .getSingleResult();
    }
}
