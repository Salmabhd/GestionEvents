package com.example.projetsdr.service;

import com.example.projetsdr.model.Event;
import com.example.projetsdr.repository.EventDataRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.List;

@Stateless
public class EventDisplayService {

    @Inject
    private EventDataRepository repository;

    public List<Event> getAllEvents() {
        return repository.findAll();
    }
}
