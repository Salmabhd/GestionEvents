package com.example.projetsdr.service;

import com.example.projetsdr.model.Event;
import com.example.projetsdr.repository.EventAddRepository;
import java.time.LocalDateTime;
import java.util.List;

public class EventAddService {
    private final EventAddRepository eventRepository;

    public EventAddService(EventAddRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event createEvent(String title, String description, LocalDateTime eventDate,
                             String venue, String city, String category, Integer maxParticipants,
                             Double ticketPrice) throws ServiceException {
        validateEventData(title, description, eventDate, venue, maxParticipants);

        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setEventDate(eventDate);
        event.setVenue(venue);
        event.setCity(city);
        event.setCategory(category);
        event.setStatus("ACTIVE");
        event.setMaxParticipants(maxParticipants);
        event.setTicketsSold(0);
        event.setTicketPrice(ticketPrice);

        eventRepository.save(event);
        return event;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(Long id) throws ServiceException {
        if (id == null) {
            throw new ServiceException("L'ID de l'événement ne peut pas être null");
        }

        Event event = eventRepository.findById(id);
        if (event == null) {
            throw new ServiceException("Aucun événement trouvé avec l'ID: " + id);
        }
        return event;
    }

    public Event updateEvent(Long id, String title, String description, LocalDateTime eventDate,
                             String venue, String city, String category, Integer maxParticipants,
                             Double ticketPrice) throws ServiceException {
        Event existingEvent = getEventById(id);

        validateEventData(title, description, eventDate, venue, maxParticipants);

        existingEvent.setTitle(title);
        existingEvent.setDescription(description);
        existingEvent.setEventDate(eventDate);
        existingEvent.setVenue(venue);
        existingEvent.setCity(city);
        existingEvent.setCategory(category);
        existingEvent.setMaxParticipants(maxParticipants);
        existingEvent.setTicketPrice(ticketPrice);

        eventRepository.update(existingEvent);
        return existingEvent;
    }

    public void deleteEvent(Long id) throws ServiceException {
        Event event = getEventById(id);
        eventRepository.delete(id);
    }

    public boolean reserverPlaces(Long eventId, int nombrePlaces) throws ServiceException {
        Event event = getEventById(eventId);

        if (nombrePlaces <= 0) {
            throw new ServiceException("Le nombre de places à réserver doit être positif");
        }

        int ticketsSold = event.getTicketsSold() != null ? event.getTicketsSold() : 0;
        int maxParticipants = event.getMaxParticipants();

        if (ticketsSold + nombrePlaces > maxParticipants) {
            throw new ServiceException("Pas assez de places disponibles. Places restantes: " +
                    (maxParticipants - ticketsSold));
        }

        event.setTicketsSold(ticketsSold + nombrePlaces);
        eventRepository.update(event);
        return true;
    }

    public void annulerReservations(Long eventId, int nombrePlaces) throws ServiceException {
        Event event = getEventById(eventId);

        if (nombrePlaces <= 0) {
            throw new ServiceException("Le nombre de places à annuler doit être positif");
        }

        int ticketsSold = event.getTicketsSold() != null ? event.getTicketsSold() : 0;

        if (nombrePlaces > ticketsSold) {
            throw new ServiceException("Impossible d'annuler plus de places que celles réservées");
        }

        event.setTicketsSold(ticketsSold - nombrePlaces);
        eventRepository.update(event);
    }

    public List<Event> getUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        return eventRepository.findAll().stream()
                .filter(event -> event.getEventDate().isAfter(now))
                .sorted((e1, e2) -> e1.getEventDate().compareTo(e2.getEventDate()))
                .toList();
    }

    public List<Event> getEventsByCategory(String category) {
        return eventRepository.findAll().stream()
                .filter(event -> category.equals(event.getCategory()))
                .toList();
    }

    public List<Event> getEventsByStatus(String status) {
        return eventRepository.findAll().stream()
                .filter(event -> status.equals(event.getStatus()))
                .toList();
    }

    public List<Event> getEventsByCity(String city) {
        return eventRepository.findAll().stream()
                .filter(event -> city.equals(event.getCity()))
                .toList();
    }

    public int getAvailableTickets(Long eventId) throws ServiceException {
        Event event = getEventById(eventId);
        int ticketsSold = event.getTicketsSold() != null ? event.getTicketsSold() : 0;
        return event.getMaxParticipants() - ticketsSold;
    }

    private void validateEventData(String title, String description, LocalDateTime eventDate,
                                   String venue, Integer maxParticipants) throws ServiceException {
        if (title == null || title.trim().isEmpty()) {
            throw new ServiceException("Le titre est obligatoire");
        }

        if (eventDate == null) {
            throw new ServiceException("La date et heure sont obligatoires");
        }

        if (eventDate.isBefore(LocalDateTime.now())) {
            throw new ServiceException("La date ne peut pas être dans le passé");
        }

        if (venue == null || venue.trim().isEmpty()) {
            throw new ServiceException("Le lieu est obligatoire");
        }

        if (maxParticipants == null || maxParticipants <= 0) {
            throw new ServiceException("Le nombre maximum de participants doit être positif");
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