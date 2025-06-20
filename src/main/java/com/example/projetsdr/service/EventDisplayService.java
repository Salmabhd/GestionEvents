package com.example.projetsdr.service;

import com.example.projetsdr.model.EventEntity;
import com.example.projetsdr.repository.EventDataRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.List;

@Stateless
public class EventDisplayService {

    @Inject
    private EventDataRepository repository;

    public List<EventEntity> getAllEvents() {
        return repository.findAll();
    }
}