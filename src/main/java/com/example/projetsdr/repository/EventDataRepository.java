package com.example.projetsdr.repository;

import com.example.projetsdr.model.Event;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class EventDataRepository {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    public List<Event> findAll() {
        return em.createQuery("SELECT e FROM Event e", Event.class).getResultList();
    }
}
