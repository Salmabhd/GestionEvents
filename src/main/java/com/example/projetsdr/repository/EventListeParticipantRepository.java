package com.example.projetsdr.repository;

import com.example.projetsdr.model.EventParticipation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class EventListeParticipantRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<EventParticipation> findAll() {
        try {
            TypedQuery<EventParticipation> query = entityManager.createQuery(
                    "SELECT ep FROM EventParticipation ep ORDER BY ep.createdAt DESC",
                    EventParticipation.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Erreur de lecture des participations", e);
        }
    }

    public List<EventParticipation> findWithFilters(Long eventId, String email, String status) {
        try {
            StringBuilder jpql = new StringBuilder("SELECT ep FROM EventParticipation ep WHERE 1=1");

            if (eventId != null) {
                jpql.append(" AND ep.event.id = :eventId");
            }
            if (email != null && !email.trim().isEmpty()) {
                jpql.append(" AND LOWER(ep.participantEmail) LIKE LOWER(:email)");
            }
            if (status != null && !status.trim().isEmpty()) {
                jpql.append(" AND ep.status = :status");
            }

            jpql.append(" ORDER BY ep.createdAt DESC");

            TypedQuery<EventParticipation> query = entityManager.createQuery(jpql.toString(), EventParticipation.class);

            if (eventId != null) {
                query.setParameter("eventId", eventId);
            }
            if (email != null && !email.trim().isEmpty()) {
                query.setParameter("email", "%" + email.trim() + "%");
            }
            if (status != null && !status.trim().isEmpty()) {
                query.setParameter("status", status);
            }

            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Erreur de filtrage des participations", e);
        }
    }

    public EventParticipation findById(Long id) {
        try {
            return entityManager.find(EventParticipation.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche du participant avec ID: " + id, e);
        }
    }

    @Transactional
    public EventParticipation save(EventParticipation participation) {
        try {
            if (participation.getId() == null) {
                entityManager.persist(participation);
                return participation;
            } else {
                return entityManager.merge(participation);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur de sauvegarde de la participation", e);
        }
    }

    @Transactional
    public boolean deleteById(Long id) {
        try {
            EventParticipation participation = entityManager.find(EventParticipation.class, id);
            if (participation != null) {
                entityManager.remove(participation);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Erreur de suppression de la participation avec ID: " + id, e);
        }
    }

    /**
     * Supprime tous les participants
     */
    @Transactional
    public int deleteAll() {
        try {
            int deletedCount = entityManager.createQuery("DELETE FROM EventParticipation").executeUpdate();
            entityManager.flush();
            return deletedCount;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression de tous les participants", e);
        }
    }

    /**
     * Compte le nombre total de participants
     */
    public long countAll() {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(ep) FROM EventParticipation ep", Long.class);
            return query.getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du comptage des participants", e);
        }
    }

    /**
     * Trouve les participations par email
     */
    public List<EventParticipation> findByEmail(String email) {
        try {
            TypedQuery<EventParticipation> query = entityManager.createQuery(
                    "SELECT ep FROM EventParticipation ep WHERE LOWER(ep.participantEmail) = LOWER(:email)",
                    EventParticipation.class);
            query.setParameter("email", email);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche par email: " + email, e);
        }
    }

    /**
     * Trouve les participations par événement
     */
    public List<EventParticipation> findByEventId(Long eventId) {
        try {
            TypedQuery<EventParticipation> query = entityManager.createQuery(
                    "SELECT ep FROM EventParticipation ep WHERE ep.event.id = :eventId ORDER BY ep.createdAt DESC",
                    EventParticipation.class);
            query.setParameter("eventId", eventId);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche par événement ID: " + eventId, e);
        }
    }

    /**
     * Trouve les participations par statut
     */
    public List<EventParticipation> findByStatus(String status) {
        try {
            TypedQuery<EventParticipation> query = entityManager.createQuery(
                    "SELECT ep FROM EventParticipation ep WHERE ep.status = :status ORDER BY ep.createdAt DESC",
                    EventParticipation.class);
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche par statut: " + status, e);
        }
    }

    /**
     * Vérifie si un participant existe
     */
    public boolean existsById(Long id) {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(ep) FROM EventParticipation ep WHERE ep.id = :id", Long.class);
            query.setParameter("id", id);
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}