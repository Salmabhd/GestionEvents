package com.example.projetsdr.service;

import com.example.projetsdr.model.Event;
import com.example.projetsdr.model.Event.EventCategory;
import com.example.projetsdr.model.Event.EventStatus;
import com.example.projetsdr.repository.EventAddRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class EventAddService {
    private static final Logger LOGGER = Logger.getLogger(EventAddService.class.getName());

    @Inject
    private EventAddRepository eventAddRepository;

    // Cache simple pour éviter les requêtes répétées
    private List<Event> cachedEvents;
    private LocalDateTime lastCacheUpdate;
    private static final long CACHE_DURATION_MINUTES = 1; // Cache de 1 minute

    public EventAddService() {
    }

    public EventAddService(EventAddRepository eventAddRepository) {
        this.eventAddRepository = eventAddRepository;
    }

    public Event saveEvent(Event event) throws ServiceException {
        try {
            validateEventData(event);
            Event savedEvent = eventAddRepository.save(event);
            // Invalide le cache après ajout
            invalidateCache();
            return savedEvent;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la sauvegarde", e);
            throw new ServiceException("Erreur lors de la sauvegarde: " + e.getMessage(), e);
        }
    }

    public Event createEvent(Event event) throws ServiceException {
        validateEventData(event);

        if (event.getCreatedAt() == null) {
            event.setCreatedAt(LocalDateTime.now());
        }
        if (event.getUpdatedAt() == null) {
            event.setUpdatedAt(LocalDateTime.now());
        }

        Event savedEvent = eventAddRepository.save(event);

        // Invalide le cache pour forcer le rechargement
        invalidateCache();

        LOGGER.info("Événement créé avec succès : " + savedEvent.getTitle());
        return savedEvent;
    }

    public List<Event> getAllEvents() {
        // Utilise le cache si disponible et valide
        if (isCacheValid()) {
            LOGGER.info("Retour des événements depuis le cache");
            return cachedEvents;
        }

        LOGGER.info("Rechargement des événements depuis la base de données");
        cachedEvents = eventAddRepository.findAll();
        lastCacheUpdate = LocalDateTime.now();
        return cachedEvents;
    }

    // Méthode pour forcer le rechargement (utile pour les tests)
    public List<Event> getAllEventsForceRefresh() {
        invalidateCache();
        return getAllEvents();
    }

    public Event getEventById(Long id) throws ServiceException {
        if (id == null) {
            throw new ServiceException("L'ID de l'événement ne peut pas être null");
        }

        Event event = eventAddRepository.findById(id);
        if (event == null) {
            throw new ServiceException("Aucun événement trouvé avec l'ID: " + id);
        }
        return event;
    }

    public Event updateEvent(Event event) throws ServiceException {
        validateEventData(event);

        if (event.getId() == null) {
            throw new ServiceException("L'ID de l'événement est requis pour la mise à jour");
        }

        event.setUpdatedAt(LocalDateTime.now());
        Event updatedEvent = eventAddRepository.update(event);

        // Invalide le cache après modification
        invalidateCache();

        return updatedEvent;
    }

    public void deleteEvent(Long id) throws ServiceException {
        if (id == null) {
            throw new ServiceException("L'ID de l'événement ne peut pas être null");
        }

        eventAddRepository.delete(id);

        // Invalide le cache après suppression
        invalidateCache();
    }

    public void reserveTickets(Long eventId, int quantity) throws ServiceException {
        Event event = getEventById(eventId);

        if (quantity <= 0) {
            throw new ServiceException("La quantité doit être positive");
        }

        if (event.getMaxParticipants() != null &&
                (event.getTicketsSold() + quantity) > event.getMaxParticipants()) {
            throw new ServiceException("Pas assez de places disponibles");
        }

        event.setTicketsSold(event.getTicketsSold() + quantity);
        eventAddRepository.update(event);

        // Invalide le cache après réservation
        invalidateCache();
    }

    public List<Event> getUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        return getAllEvents().stream()
                .filter(event -> event.getEventDate() != null && event.getEventDate().isAfter(now))
                .sorted((e1, e2) -> e1.getEventDate().compareTo(e2.getEventDate()))
                .collect(Collectors.toList());
    }

    public List<Event> getEventsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return getAllEvents();
        }

        try {
            EventCategory cat = EventCategory.valueOf(category.toUpperCase());
            return getAllEvents().stream()
                    .filter(event -> cat.equals(event.getCategory()))
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Catégorie inconnue : " + category);
            return List.of();
        }
    }

    public List<Event> getEventsByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return getAllEvents();
        }

        try {
            EventStatus st = EventStatus.valueOf(status.toUpperCase());
            return getAllEvents().stream()
                    .filter(event -> st.equals(event.getStatus()))
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Statut inconnu : " + status);
            return List.of();
        }
    }

    // Méthodes de gestion du cache
    private boolean isCacheValid() {
        return cachedEvents != null &&
                lastCacheUpdate != null &&
                lastCacheUpdate.plusMinutes(CACHE_DURATION_MINUTES).isAfter(LocalDateTime.now());
    }

    private void invalidateCache() {
        cachedEvents = null;
        lastCacheUpdate = null;
        LOGGER.info("Cache des événements invalidé");
    }

    // Méthode publique pour invalider le cache manuellement
    public void refreshCache() {
        invalidateCache();
    }

    private void validateEventData(Event event) throws ServiceException {
        if (event == null) {
            throw new ServiceException("L'événement ne peut pas être null");
        }

        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            throw new ServiceException("Le titre est obligatoire");
        }

        if (event.getEventDate() == null) {
            throw new ServiceException("La date et heure sont obligatoires");
        }

        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ServiceException("La date ne peut pas être dans le passé");
        }

        if (event.getVenue() == null || event.getVenue().trim().isEmpty()) {
            throw new ServiceException("Le lieu est obligatoire");
        }

        if (event.getCity() == null || event.getCity().trim().isEmpty()) {
            throw new ServiceException("La ville est obligatoire");
        }

        if (event.getCategory() == null) {
            throw new ServiceException("La catégorie est obligatoire");
        }

        if (event.getStatus() == null) {
            throw new ServiceException("Le statut est obligatoire");
        }

        if (event.getMaxParticipants() != null && event.getMaxParticipants() <= 0) {
            throw new ServiceException("Le nombre maximum de participants doit être positif");
        }

        if (event.getTicketPrice() != null && event.getTicketPrice() < 0) {
            throw new ServiceException("Le prix du billet ne peut pas être négatif");
        }
    }

    public static class ServiceException extends Exception {
        public ServiceException(String message) {
            super(message);
        }

        public ServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}