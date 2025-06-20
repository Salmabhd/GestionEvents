package com.example.projetsdr.repository;

import com.example.projetsdr.model.EventEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class EventDataRepository {

    @PersistenceContext
    private EntityManager em;

    public List<EventEntity> findAll() {
        return em.createQuery("SELECT e FROM Event e", EventEntity.class).getResultList();
    }
}
