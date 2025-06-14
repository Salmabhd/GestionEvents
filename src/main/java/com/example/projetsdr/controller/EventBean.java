package com.example.projetsdr.controller;

import com.example.projetsdr.model.Event;
import com.example.projetsdr.service.EventService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

@Named
@SessionScoped
public class EventBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EventService eventService;

    // Propriétés pour l'événement courant
    private Event currentEvent;

    // Listes pour l'affichage
    private List<Event> events;
    private List<Event> filteredEvents;

    // Propriétés pour la recherche et les filtres
    private String searchTitle;
    private String searchCity;
    private Event.EventCategory selectedCategory;
    private Event.EventStatus selectedStatus;

    // Propriétés pour la gestion des tickets
    private int ticketsToSell;

    // Propriétés pour l'affichage des statistiques
    private EventService.EventStatistics statistics;

    @PostConstruct
    public void init() {
        refreshEventList();
        resetCurrentEvent();
        loadStatistics();
    }

    // Actions CRUD
    public String createEvent() {
        try {
            eventService.createEvent(currentEvent);
            addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Événement créé avec succès");
            refreshEventList();
            resetCurrentEvent();
            return "event-list?faces-redirect=true";
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage());
            return null;
        }
    }

    public String updateEvent() {
        try {
            eventService.updateEvent(currentEvent);
            addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Événement mis à jour avec succès");
            refreshEventList();
            return "event-list?faces-redirect=true";
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage());
            return null;
        }
    }

    public void deleteEvent(Long eventId) {
        try {
            eventService.deleteEvent(eventId);
            addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Événement supprimé avec succès");
            refreshEventList();
            loadStatistics();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage());
        }
    }

    public void cancelEvent(Long eventId) {
        try {
            eventService.cancelEvent(eventId);
            addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Événement annulé avec succès");
            refreshEventList();
            loadStatistics();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage());
        }
    }

    // Actions de navigation
    // Ajoutez cette méthode dans votre EventBean

    public String editEvent() {
        try {
            eventService.updateEvent(currentEvent);
            addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Événement modifié avec succès");
            refreshEventList();
            return "event-list?faces-redirect=true";
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage());
            return null;
        }
    }

    // Modifiez votre méthode editEvent(Long eventId) existante pour qu'elle ne redirige plus
    public void loadEventForEdit(Long eventId) {
        try {
            currentEvent = eventService.findById(eventId).orElse(null);
            if (currentEvent == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Événement non trouvé");
            }
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage());
        }
    }

    public String viewEvent(Long eventId) {
        try {
            currentEvent = eventService.findById(eventId).orElse(null);
            if (currentEvent == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Événement non trouvé");
                return null;
            }
            return "event-detail?faces-redirect=true";
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage());
            return null;
        }
    }

    public String newEvent() {
        resetCurrentEvent();
        return "event-form?faces-redirect=true";
    }

    // Actions de gestion des tickets
    public void sellTickets(Long eventId) {
        try {
            eventService.sellTickets(eventId, ticketsToSell);
            addMessage(FacesMessage.SEVERITY_INFO, "Succès",
                    ticketsToSell + " ticket(s) vendu(s) avec succès");
            refreshEventList();
            ticketsToSell = 0;
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage());
        }
    }

    // Actions de recherche et filtrage
    public void searchEvents() {
        try {
            if (searchTitle != null && !searchTitle.trim().isEmpty()) {
                filteredEvents = eventService.searchEventsByTitle(searchTitle);
            } else if (searchCity != null && !searchCity.trim().isEmpty()) {
                filteredEvents = eventService.findEventsByCity(searchCity);
            } else if (selectedCategory != null) {
                filteredEvents = eventService.findEventsByCategory(selectedCategory);
            } else if (selectedStatus != null) {
                filteredEvents = eventService.findEventsByStatus(selectedStatus);
            } else {
                filteredEvents = events;
            }
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage());
        }
    }

    public void clearFilters() {
        searchTitle = null;
        searchCity = null;
        selectedCategory = null;
        selectedStatus = null;
        filteredEvents = events;
    }

    public void loadUpcomingEvents() {
        try {
            filteredEvents = eventService.findUpcomingEvents();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage());
        }
    }

    public void loadPopularEvents() {
        try {
            filteredEvents = eventService.findMostPopularEvents(10);
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage());
        }
    }

    // Méthodes utilitaires
    private void refreshEventList() {
        try {
            events = eventService.findAllEvents();
            filteredEvents = events;
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur lors du chargement des événements");
        }
    }

    private void resetCurrentEvent() {
        currentEvent = new Event();
        currentEvent.setStatus(Event.EventStatus.UPCOMING);
        currentEvent.setCategory(Event.EventCategory.BUSINESS);
        currentEvent.setEventDate(LocalDateTime.now().plusDays(1));
    }

    private void loadStatistics() {
        try {
            statistics = eventService.getEventStatistics();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur lors du chargement des statistiques");
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, summary, detail));
    }

    // Getters et Setters
    public Event getCurrentEvent() { return currentEvent; }
    public void setCurrentEvent(Event currentEvent) { this.currentEvent = currentEvent; }

    public List<Event> getEvents() { return events; }
    public void setEvents(List<Event> events) { this.events = events; }

    public List<Event> getFilteredEvents() { return filteredEvents; }
    public void setFilteredEvents(List<Event> filteredEvents) { this.filteredEvents = filteredEvents; }

    public String getSearchTitle() { return searchTitle; }
    public void setSearchTitle(String searchTitle) { this.searchTitle = searchTitle; }

    public String getSearchCity() { return searchCity; }
    public void setSearchCity(String searchCity) { this.searchCity = searchCity; }

    public Event.EventCategory getSelectedCategory() { return selectedCategory; }
    public void setSelectedCategory(Event.EventCategory selectedCategory) { this.selectedCategory = selectedCategory; }

    public Event.EventStatus getSelectedStatus() { return selectedStatus; }
    public void setSelectedStatus(Event.EventStatus selectedStatus) { this.selectedStatus = selectedStatus; }

    public int getTicketsToSell() { return ticketsToSell; }
    public void setTicketsToSell(int ticketsToSell) { this.ticketsToSell = ticketsToSell; }

    public EventService.EventStatistics getStatistics() { return statistics; }
    public void setStatistics(EventService.EventStatistics statistics) { this.statistics = statistics; }

    // Méthodes pour les selectItems dans les formulaires JSF
    public List<Event.EventCategory> getEventCategories() {
        return Arrays.asList(Event.EventCategory.values());
    }

    public List<Event.EventStatus> getEventStatuses() {
        return Arrays.asList(Event.EventStatus.values());
    }

    // Méthodes utilitaires pour les vues
    public boolean isEventEditable(Event event) {
        return event.getStatus() != Event.EventStatus.COMPLETED &&
                event.getStatus() != Event.EventStatus.CANCELLED;
    }

    public boolean canSellTickets(Event event) {
        return event.getStatus() == Event.EventStatus.ACTIVE &&
                !event.isSoldOut() &&
                event.getEventDate().isAfter(LocalDateTime.now());
    }

    public String getEventStatusClass(Event.EventStatus status) {
        switch (status) {
            case ACTIVE: return "badge-success";
            case UPCOMING: return "badge-info";
            case COMPLETED: return "badge-secondary";
            case CANCELLED: return "badge-danger";
            default: return "badge-light";
        }
    }

    public String getCategoryClass(Event.EventCategory category) {
        switch (category) {
            case BUSINESS: return "category-business";
            case ENTERTAINMENT: return "category-entertainment";
            case EDUCATION: return "category-education";
            case SPORT: return "category-sport";
            case SOCIAL: return "category-social";
            case TECHNOLOGY: return "category-technology";
            default: return "category-default";
        }
    }
    public String formatEventDate(Event event) {
        if (event == null || event.getEventDate() == null) {
            return "Date non définie";
        }

        try {
            // If eventDate is LocalDateTime
            if (event.getEventDate() instanceof LocalDateTime) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
                return ((LocalDateTime) event.getEventDate()).format(formatter);
            }

            // If eventDate is java.util.Date

            // Fallback - try to convert to string
            return event.getEventDate().toString();

        } catch (Exception e) {
            return "Date invalide";
        }
    }
    // Getter pour la date formatée
    public String getEventDateFormatted() {
        if (currentEvent == null || currentEvent.getEventDate() == null) {
            return "";
        }
        return currentEvent.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }

    // Setter pour la date depuis le formulaire
    public void setEventDateFormatted(String dateStr) {
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                currentEvent.setEventDate(LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
            } catch (Exception e) {
                // Gérer l'erreur si nécessaire
                System.out.println("Erreur de conversion de date: " + e.getMessage());
            }
        }
    }
}