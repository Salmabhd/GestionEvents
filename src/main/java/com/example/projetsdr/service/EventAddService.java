package com.example.projetsdr.service;

import com.example.projetsdr.model.Event;
import com.example.projetsdr.repository.EventAddRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EventAddService {
    private static final Logger LOGGER = Logger.getLogger(EventAddService.class.getName());

    @Inject
    private EventAddRepository eventAddRepository;

    // Default constructor required for CDI
    public EventAddService() {
    }

    // Keep the existing constructor for manual instantiation if needed
    public EventAddService(EventAddRepository eventAddRepository) {
        this.eventAddRepository = eventAddRepository;
    }

    public boolean saveEvent(Event event) {
        try {
            validateEventData(event);
            return eventAddRepository.save(event);
        } catch (ServiceException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la sauvegarde", e);
            return false;
        }
    }

    public Event createEvent(Event event) throws ServiceException {
        validateEventData(event);

        // S'assurer que les timestamps sont définis
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(LocalDateTime.now());
        }
        if (event.getUpdatedAt() == null) {
            event.setUpdatedAt(LocalDateTime.now());
        }

        boolean saved = eventAddRepository.save(event);
        if (!saved) {
            throw new ServiceException("Impossible de sauvegarder l'événement");
        }

        return event;
    }

    public List<Event> getAllEvents() {
        return eventAddRepository.findAll();
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

    public boolean updateEvent(Event event) throws ServiceException {
        validateEventData(event);
        if (event.getId() == null) {
            throw new ServiceException("L'ID de l'événement est requis pour la mise à jour");
        }

        // Mettre à jour le timestamp
        event.setUpdatedAt(LocalDateTime.now());

        return eventAddRepository.update(event);
    }

    public boolean deleteEvent(Long id) throws ServiceException {
        if (id == null) {
            throw new ServiceException("L'ID de l'événement ne peut pas être null");
        }
        return eventAddRepository.delete(id);
    }

    public boolean reserveTickets(Long eventId, int quantity) throws ServiceException {
        Event event = getEventById(eventId);

        if (quantity <= 0) {
            throw new ServiceException("La quantité doit être positive");
        }

        if (event.getMaxParticipants() != null &&
                (event.getTicketsSold() + quantity) > event.getMaxParticipants()) {
            throw new ServiceException("Pas assez de places disponibles");
        }

        event.setTicketsSold(event.getTicketsSold() + quantity);
        return eventAddRepository.update(event);
    }

    public List<Event> getUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        return eventAddRepository.findAll().stream()
                .filter(event -> event.getEventDate() != null && event.getEventDate().isAfter(now))
                .sorted((e1, e2) -> e1.getEventDate().compareTo(e2.getEventDate()))
                .collect(Collectors.toList());
    }

    public List<Event> getEventsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return getAllEvents();
        }

        return eventAddRepository.findAll().stream()
                .filter(event -> category.equalsIgnoreCase(event.getCategory()))
                .collect(Collectors.toList());
    }

    public List<Event> getEventsByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return getAllEvents();
        }

        return eventAddRepository.findAll().stream()
                .filter(event -> status.equalsIgnoreCase(event.getStatus()))
                .collect(Collectors.toList());
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

        if (event.getCategory() == null || event.getCategory().trim().isEmpty()) {
            throw new ServiceException("La catégorie est obligatoire");
        }

        if (event.getStatus() == null || event.getStatus().trim().isEmpty()) {
            throw new ServiceException("Le statut est obligatoire");
        }

        // Validation des valeurs de catégorie (optionnel, pour plus de sécurité)
        validateCategory(event.getCategory());

        // Validation des valeurs de statut (optionnel, pour plus de sécurité)
        validateStatus(event.getStatus());

        if (event.getMaxParticipants() != null && event.getMaxParticipants() <= 0) {
            throw new ServiceException("Le nombre maximum de participants doit être positif");
        }

        if (event.getTicketPrice() != null && event.getTicketPrice() < 0) {
            throw new ServiceException("Le prix du billet ne peut pas être négatif");
        }
    }

    private void validateCategory(String category) throws ServiceException {
        if (category == null) return;

        String[] validCategories = {
                "TECHNOLOGY", "ENTERTAINMENT", "BUSINESS", "SPORT",
                "EDUCATION", "SOCIAL", "ART", "FOOD", "OTHER"
        };

        boolean isValid = false;
        for (String validCategory : validCategories) {
            if (validCategory.equalsIgnoreCase(category)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            throw new ServiceException("Catégorie invalide: " + category);
        }
    }

    private void validateStatus(String status) throws ServiceException {
        if (status == null) return;

        String[] validStatuses = {
                "PLANNED", "ACTIVE", "COMPLETED", "CANCELLED"
        };

        boolean isValid = false;
        for (String validStatus : validStatuses) {
            if (validStatus.equalsIgnoreCase(status)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            throw new ServiceException("Statut invalide: " + status);
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