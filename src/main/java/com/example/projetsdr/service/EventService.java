package com.example.projetsdr.service;


import com.example.projetsdr.model.Event;
import com.example.projetsdr.model.Event.EventCategory;
import com.example.projetsdr.model.Event.EventStatus;
import com.example.projetsdr.repository.EventRepository;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Transactional
public class EventService {

    @Inject
    private EventRepository eventRepository;

    // Méthodes CRUD
    public Event createEvent(Event event) {
        validateEvent(event);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    public Event updateEvent(Event event) {
        validateEvent(event);
        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    // Méthodes de recherche et filtrage
    public List<Event> getEventsByCategory(EventCategory category) {
        return eventRepository.findByCategory(category);
    }

    public List<Event> getEventsByStatus(EventStatus status) {
        return eventRepository.findByStatus(status);
    }

    public List<Event> searchEvents(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllEvents();
        }
        return eventRepository.searchByTitle(searchTerm);
    }

    public List<Event> getEventsWithFilters(String categoryStr, String statusStr,
                                            String dateFilter, String searchTerm) {
        EventCategory category = null;
        EventStatus status = null;

        // Conversion des paramètres
        if (categoryStr != null && !categoryStr.isEmpty() && !"all".equals(categoryStr)) {
            try {
                category = EventCategory.valueOf(categoryStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignorer les valeurs invalides
            }
        }

        if (statusStr != null && !statusStr.isEmpty() && !"all".equals(statusStr)) {
            try {
                status = EventStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignorer les valeurs invalides
            }
        }

        return eventRepository.findWithFilters(category, status, dateFilter, searchTerm);
    }

    // Méthodes de statistiques
    public Map<String, Long> getEventStatistics() {
        Map<String, Long> stats = new HashMap<>();

        stats.put("total", eventRepository.countAll());
        stats.put("active", eventRepository.countByStatus(EventStatus.ACTIVE));
        stats.put("upcoming", eventRepository.countByStatus(EventStatus.UPCOMING));
        stats.put("completed", eventRepository.countByStatus(EventStatus.COMPLETED));
        stats.put("cancelled", eventRepository.countByStatus(EventStatus.CANCELLED));

        return stats;
    }

    public Map<String, Long> getCategoryStatistics() {
        Map<String, Long> stats = new HashMap<>();

        for (EventCategory category : EventCategory.values()) {
            stats.put(category.name().toLowerCase(), eventRepository.countByCategory(category));
        }

        return stats;
    }

    // Méthodes utilitaires
    public List<Event> getUpcomingEvents(int limit) {
        return eventRepository.findUpcomingEvents(limit);
    }

    public List<Event> getActiveEvents() {
        return eventRepository.findActiveEvents();
    }

    // Gestion des billets
    public boolean reserveTickets(Long eventId, int quantity) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (!eventOpt.isPresent()) {
            return false;
        }

        Event event = eventOpt.get();
        int availableTickets = event.getAvailableTickets();

        if (availableTickets >= quantity) {
            event.setTicketsSold(event.getTicketsSold() + quantity);
            eventRepository.save(event);
            return true;
        }

        return false;
    }

    public boolean cancelTickets(Long eventId, int quantity) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (!eventOpt.isPresent()) {
            return false;
        }

        Event event = eventOpt.get();
        int currentSold = event.getTicketsSold();

        if (currentSold >= quantity) {
            event.setTicketsSold(currentSold - quantity);
            eventRepository.save(event);
            return true;
        }

        return false;
    }

    // Gestion des statuts
    public void updateEventStatus(Long eventId, EventStatus newStatus) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isPresent()) {
            Event event = eventOpt.get();
            event.setStatus(newStatus);
            eventRepository.save(event);
        }
    }

    // Méthodes de validation
    private void validateEvent(Event event) {
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre de l'événement est obligatoire");
        }

        if (event.getEventDate() == null) {
            throw new IllegalArgumentException("La date de l'événement est obligatoire");
        }

        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La date de l'événement ne peut pas être dans le passé");
        }

        if (event.getVenue() == null || event.getVenue().trim().isEmpty()) {
            throw new IllegalArgumentException("Le lieu de l'événement est obligatoire");
        }

        if (event.getCity() == null || event.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("La ville de l'événement est obligatoire");
        }

        if (event.getMaxParticipants() != null && event.getMaxParticipants() <= 0) {
            throw new IllegalArgumentException("Le nombre maximum de participants doit être positif");
        }

        if (event.getTicketsSold() != null && event.getTicketsSold() < 0) {
            throw new IllegalArgumentException("Le nombre de billets vendus ne peut pas être négatif");
        }

        if (event.getTicketPrice() != null && event.getTicketPrice() < 0) {
            throw new IllegalArgumentException("Le prix du billet ne peut pas être négatif");
        }
    }

    // Méthodes pour la gestion automatique des statuts
    public void updateExpiredEvents() {
        List<Event> activeEvents = getActiveEvents();
        LocalDateTime now = LocalDateTime.now();

        for (Event event : activeEvents) {
            if (event.getEventDate().isBefore(now)) {
                event.setStatus(EventStatus.COMPLETED);
                eventRepository.save(event);
            }
        }
    }

    // Méthode pour obtenir les événements populaires
    public List<Event> getPopularEvents(int limit) {
        return eventRepository.findAll().stream()
                .filter(event -> event.getOccupancyRate() > 70)
                .sorted((e1, e2) -> Double.compare(e2.getOccupancyRate(), e1.getOccupancyRate()))
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }
}
