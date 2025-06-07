package com.example.projetsdr.controller;

import com.example.projetsdr.model.Event;
import com.example.projetsdr.model.Event.EventCategory;
import com.example.projetsdr.model.Event.EventStatus;
import com.example.projetsdr.service.EventService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Named("eventBean")
@ViewScoped
public class EventBean implements Serializable {

    @Inject
    private EventService eventService;

    // Propriétés pour la liste des événements
    private List<Event> events;
    private List<Event> filteredEvents;

    // Propriétés pour les filtres
    private String selectedCategory = "all";
    private String selectedStatus = "all";
    private String selectedDateFilter = "all";
    private String searchTerm = "";

    // Propriétés pour les statistiques
    private Map<String, Long> eventStatistics;

    // Propriétés pour l'événement sélectionné
    private Event selectedEvent;
    private Long selectedEventId;

    // Formatters
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    @PostConstruct
    public void init() {
        loadEvents();
        loadStatistics();
    }

    // Méthodes de chargement
    public void loadEvents() {
        try {
            events = eventService.getAllEvents();
            applyFilters();
        } catch (Exception e) {
            addErrorMessage("Erreur lors du chargement des événements: " + e.getMessage());
        }
    }

    public void loadStatistics() {
        try {
            eventStatistics = eventService.getEventStatistics();
        } catch (Exception e) {
            addErrorMessage("Erreur lors du chargement des statistiques: " + e.getMessage());
        }
    }

    // Méthodes de filtrage
    public void applyFilters() {
        try {
            filteredEvents = eventService.getEventsWithFilters(
                    selectedCategory, selectedStatus, selectedDateFilter, searchTerm);
        } catch (Exception e) {
            addErrorMessage("Erreur lors de l'application des filtres: " + e.getMessage());
            filteredEvents = events;
        }
    }

    public void searchEvents() {
        applyFilters();
    }

    public void resetFilters() {
        selectedCategory = "all";
        selectedStatus = "all";
        selectedDateFilter = "all";
        searchTerm = "";
        applyFilters();
    }

    // Méthodes d'action
    public String viewEvent(Long eventId) {
        return "event-details?faces-redirect=true&id=" + eventId;
    }

    public String editEvent(Long eventId) {
        return "edit-event?faces-redirect=true&id=" + eventId;
    }

    public void deleteEvent(Long eventId) {
        try {
            eventService.deleteEvent(eventId);
            addSuccessMessage("Événement supprimé avec succès");
            loadEvents();
            loadStatistics();
        } catch (Exception e) {
            addErrorMessage("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    public String createNewEvent() {
        return "create-event?faces-redirect=true";
    }

    // Méthodes utilitaires pour l'affichage
    public String formatEventDate(Event event) {
        if (event.getEventDate() != null) {
            return event.getEventDate().format(dateFormatter);
        }
        return "";
    }

    public String getEventStatusClass(EventStatus status) {
        switch (status) {
            case ACTIVE:
                return "status-active";
            case UPCOMING:
                return "status-upcoming";
            case COMPLETED:
                return "status-completed";
            case CANCELLED:
                return "status-cancelled";
            default:
                return "";
        }
    }

    public String getCategoryIcon(EventCategory category) {
        switch (category) {
            case BUSINESS:
                return "fas fa-briefcase";
            case ENTERTAINMENT:
                return "fas fa-music";
            case EDUCATION:
                return "fas fa-graduation-cap";
            case SPORT:
                return "fas fa-running";
            case SOCIAL:
                return "fas fa-heart";
            case TECHNOLOGY:
                return "fas fa-laptop-code";
            default:
                return "fas fa-calendar";
        }
    }

    public String getCategoryGradient(EventCategory category) {
        switch (category) {
            case BUSINESS:
                return "background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);";
            case ENTERTAINMENT:
                return "background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);";
            case EDUCATION:
                return "background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);";
            case SPORT:
                return "background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);";
            case SOCIAL:
                return "background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);";
            case TECHNOLOGY:
                return "background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);";
            default:
                return "background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);";
        }
    }

    public boolean isEventSoldOut(Event event) {
        return event.isSoldOut();
    }

    public double getEventOccupancyRate(Event event) {
        return event.getOccupancyRate();
    }

    // Méthodes pour les options de sélection
    public EventCategory[] getAvailableCategories() {
        return EventCategory.values();
    }

    public EventStatus[] getAvailableStatuses() {
        return EventStatus.values();
    }

    // Méthodes utilitaires pour les messages
    private void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", message));
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", message));
    }

    private void addWarningMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Avertissement", message));
    }

    // Getters et Setters
    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<Event> getFilteredEvents() {
        return filteredEvents;
    }

    public void setFilteredEvents(List<Event> filteredEvents) {
        this.filteredEvents = filteredEvents;
    }

    public String getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(String selectedCategory) {
        this.selectedCategory = selectedCategory;
        applyFilters();
    }

    public String getSelectedStatus() {
        return selectedStatus;
    }

    public void setSelectedStatus(String selectedStatus) {
        this.selectedStatus = selectedStatus;
        applyFilters();
    }

    public String getSelectedDateFilter() {
        return selectedDateFilter;
    }

    public void setSelectedDateFilter(String selectedDateFilter) {
        this.selectedDateFilter = selectedDateFilter;
        applyFilters();
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public Map<String, Long> getEventStatistics() {
        return eventStatistics;
    }

    public void setEventStatistics(Map<String, Long> eventStatistics) {
        this.eventStatistics = eventStatistics;
    }

    public Event getSelectedEvent() {
        return selectedEvent;
    }

    public void setSelectedEvent(Event selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    public Long getSelectedEventId() {
        return selectedEventId;
    }

    public void setSelectedEventId(Long selectedEventId) {
        this.selectedEventId = selectedEventId;
    }
}