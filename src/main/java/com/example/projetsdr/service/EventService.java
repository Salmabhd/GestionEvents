package com.example.projetsdr.service;

import com.example.projetsdr.model.Event;
import com.example.projetsdr.repository.EventRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class EventService {

    @Inject
    private EventRepository eventRepository;

    // Créer un nouvel événement
    public Event createEvent(Event event) {
        validateEvent(event);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    // Mettre à jour un événement existant
    public Event updateEvent(Event event) {
        if (event.getId() == null) {
            throw new IllegalArgumentException("L'ID de l'événement ne peut pas être null pour une mise à jour");
        }

        Optional<Event> existingEvent = eventRepository.findById(event.getId());
        if (existingEvent.isEmpty()) {
            throw new IllegalArgumentException("Événement non trouvé avec l'ID: " + event.getId());
        }

        validateEvent(event);
        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    // Trouver un événement par ID
    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    // Trouver tous les événements
    public List<Event> findAllEvents() {
        return eventRepository.findAll();
    }

    // Trouver les événements par statut
    public List<Event> findEventsByStatus(Event.EventStatus status) {
        return eventRepository.findByStatus(status);
    }

    // Trouver les événements par catégorie
    public List<Event> findEventsByCategory(Event.EventCategory category) {
        return eventRepository.findByCategory(category);
    }

    // Trouver les événements par ville
    public List<Event> findEventsByCity(String city) {
        return eventRepository.findByCity(city);
    }

    // Trouver les événements à venir
    public List<Event> findUpcomingEvents() {
        return eventRepository.findUpcomingEvents();
    }

    // Rechercher des événements par titre
    public List<Event> searchEventsByTitle(String title) {
        return eventRepository.searchByTitle(title);
    }

    // Trouver les événements par plage de dates
    public List<Event> findEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return eventRepository.findByDateRange(startDate, endDate);
    }

    // Supprimer un événement
    public void deleteEvent(Long id) {
        Optional<Event> event = eventRepository.findById(id);
        if (event.isEmpty()) {
            throw new IllegalArgumentException("Événement non trouvé avec l'ID: " + id);
        }
        eventRepository.delete(id);
    }

    // Annuler un événement
    public Event cancelEvent(Long id) {
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isEmpty()) {
            throw new IllegalArgumentException("Événement non trouvé avec l'ID: " + id);
        }

        Event event = eventOpt.get();
        event.setStatus(Event.EventStatus.CANCELLED);
        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    // Vendre des tickets
    public Event sellTickets(Long eventId, int numberOfTickets) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            throw new IllegalArgumentException("Événement non trouvé avec l'ID: " + eventId);
        }

        Event event = eventOpt.get();

        if (event.getStatus() == Event.EventStatus.CANCELLED) {
            throw new IllegalStateException("Impossible de vendre des tickets pour un événement annulé");
        }

        if (event.getMaxParticipants() != null) {
            int availableTickets = event.getAvailableTickets();
            if (numberOfTickets > availableTickets) {
                throw new IllegalStateException("Pas assez de tickets disponibles. Disponibles: " + availableTickets);
            }
        }

        event.setTicketsSold(event.getTicketsSold() + numberOfTickets);
        event.setUpdatedAt(LocalDateTime.now());

        // Marquer comme complet si tous les tickets sont vendus
        if (event.isSoldOut()) {
            event.setStatus(Event.EventStatus.COMPLETED);
        }

        return eventRepository.save(event);
    }

    // Obtenir les statistiques des événements
    public EventStatistics getEventStatistics() {
        long totalEvents = eventRepository.count();
        long activeEvents = eventRepository.countByStatus(Event.EventStatus.ACTIVE);
        long upcomingEvents = eventRepository.countByStatus(Event.EventStatus.UPCOMING);
        long completedEvents = eventRepository.countByStatus(Event.EventStatus.COMPLETED);
        long cancelledEvents = eventRepository.countByStatus(Event.EventStatus.CANCELLED);

        return new EventStatistics(totalEvents, activeEvents, upcomingEvents, completedEvents, cancelledEvents);
    }

    // Trouver les événements les plus populaires
    public List<Event> findMostPopularEvents(int limit) {
        return eventRepository.findMostPopularEvents(limit);
    }

    // Valider un événement
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

        if (event.getCategory() == null) {
            throw new IllegalArgumentException("La catégorie de l'événement est obligatoire");
        }

        if (event.getStatus() == null) {
            throw new IllegalArgumentException("Le statut de l'événement est obligatoire");
        }

        if (event.getMaxParticipants() != null && event.getMaxParticipants() <= 0) {
            throw new IllegalArgumentException("Le nombre maximum de participants doit être positif");
        }

        if (event.getTicketPrice() != null && event.getTicketPrice() < 0) {
            throw new IllegalArgumentException("Le prix du ticket ne peut pas être négatif");
        }
    }

    // Classe interne pour les statistiques
    public static class EventStatistics {
        private final long totalEvents;
        private final long activeEvents;
        private final long upcomingEvents;
        private final long completedEvents;
        private final long cancelledEvents;

        public EventStatistics(long totalEvents, long activeEvents, long upcomingEvents,
                               long completedEvents, long cancelledEvents) {
            this.totalEvents = totalEvents;
            this.activeEvents = activeEvents;
            this.upcomingEvents = upcomingEvents;
            this.completedEvents = completedEvents;
            this.cancelledEvents = cancelledEvents;
        }

        public long getTotalEvents() { return totalEvents; }
        public long getActiveEvents() { return activeEvents; }
        public long getUpcomingEvents() { return upcomingEvents; }
        public long getCompletedEvents() { return completedEvents; }
        public long getCancelledEvents() { return cancelledEvents; }
    }
}