package com.example.projetsdr.repository;

import com.example.projetsdr.model.EventParticipation;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class EventParticipationRepository {

    @PersistenceContext
    private EntityManager em;

    public void save(EventParticipation participation) {
        em.persist(participation);
    }

    public boolean alreadyExists(Long eventId, String email) {
        Long count = em.createQuery(
                        "SELECT COUNT(p) FROM EventParticipation p WHERE p.event.id = :eventId AND p.participantEmail = :email", Long.class)
                .setParameter("eventId", eventId)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }
}
