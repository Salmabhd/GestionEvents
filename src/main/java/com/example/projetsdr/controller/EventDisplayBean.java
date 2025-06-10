package com.example.projetsdr.controller;

import com.example.projetsdr.model.EventEntity;
import com.example.projetsdr.service.EventDisplayService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class EventDisplayBean implements Serializable {

    private List<EventEntity> events;

    @Inject
    private EventDisplayService eventService;

    @PostConstruct
    public void init() {
        events = eventService.getAllEvents();
    }

    public List<EventEntity> getEvents() {
        return events;
    }

    public void reserver(EventEntity event) {
        System.out.println("Réservation : " + event.getTitle());
        // Logique de réservation ici
    }
}
