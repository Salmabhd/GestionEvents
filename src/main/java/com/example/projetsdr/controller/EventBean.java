package com.example.projetsdr.controller;

import com.example.projetsdr.model.Event;
import com.example.projetsdr.service.EventService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

@Named
@SessionScoped
public class EventBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EventService eventService;

    // Propriétés pour l'événement courant
    private Event currentEvent;

    // Propriétés temporaires pour la gestion séparée de la date et de l'heure
    private String tempDate;
    private String tempTime;

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
    private String eventDateOnly;

    @PostConstruct
    public void init() {
        refreshEventList();
        resetCurrentEvent();
        loadStatistics();
    }

    // MÉTHODES POUR LA GESTION DES DATES ET HEURES - CORRIGÉES
    public String getEventDateOnly() {
        // Priorité aux valeurs temporaires si elles existent
        if (tempDate != null && !tempDate.trim().isEmpty()) {
            return tempDate;
        }

        // Sinon, extraire de l'événement courant
        if (currentEvent != null && currentEvent.getEventDate() != null) {
            String dateStr = currentEvent.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            tempDate = dateStr; // Synchroniser avec la valeur temporaire
            return dateStr;
        }

        return "";
    }

    public void setEventDateOnly(String dateOnly) {
        System.out.println("setEventDateOnly appelé avec: " + dateOnly);
        this.tempDate = dateOnly;
        updateEventDateTime();
    }

    public String getEventTimeOnly() {
        // Priorité aux valeurs temporaires si elles existent
        if (tempTime != null && !tempTime.trim().isEmpty()) {
            return tempTime;
        }

        // Sinon, extraire de l'événement courant
        if (currentEvent != null && currentEvent.getEventDate() != null) {
            String timeStr = currentEvent.getEventDate().format(DateTimeFormatter.ofPattern("HH:mm"));
            tempTime = timeStr; // Synchroniser avec la valeur temporaire
            return timeStr;
        }

        return "";
    }

    public void setEventTimeOnly(String timeOnly) {
        System.out.println("setEventTimeOnly appelé avec: " + timeOnly);
        this.tempTime = timeOnly;
        updateEventDateTime();
    }

    // Méthode pour combiner date et heure en LocalDateTime - AMÉLIORÉE
    private void updateEventDateTime() {
        if (tempDate != null && !tempDate.trim().isEmpty() &&
                tempTime != null && !tempTime.trim().isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(tempDate);
                LocalTime time = LocalTime.parse(tempTime);
                LocalDateTime dateTime = LocalDateTime.of(date, time);

                if (currentEvent == null) {
                    currentEvent = new Event();
                }
                currentEvent.setEventDate(dateTime);
                System.out.println("Date/heure combinée: " + dateTime);
            } catch (DateTimeParseException e) {
                System.err.println("Erreur de parsing date/heure: " + e.getMessage());
                addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Format de date ou heure invalide");
            }
        }
    }

    // Méthodes de validation
    public void validateDate(FacesContext context, UIComponent component, Object value)
            throws ValidatorException {
        if (value == null) return;

        String dateStr = value.toString();
        try {
            LocalDate date = LocalDate.parse(dateStr);
            // Validation business : la date ne peut pas être dans le passé
            if (date.isBefore(LocalDate.now())) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Erreur", "La date de l'événement ne peut pas être dans le passé");
                throw new ValidatorException(message);
            }
        } catch (DateTimeParseException e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Erreur", "Format de date invalide. Utilisez le format YYYY-MM-DD");
            throw new ValidatorException(message);
        }
    }

    public void validateTime(FacesContext context, UIComponent component, Object value)
            throws ValidatorException {
        if (value == null) return;

        String timeStr = value.toString();
        try {
            LocalTime.parse(timeStr);
        } catch (DateTimeParseException e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Erreur", "Format d'heure invalide. Utilisez le format HH:MM");
            throw new ValidatorException(message);
        }
    }

    // ACTIONS CRUD
    public String createEvent() {
        try {
            // S'assurer que la date/heure est bien définie
            updateEventDateTime();

            if (currentEvent.getEventDate() == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Date et heure obligatoires");
                return null;
            }

            eventService.createEvent(currentEvent);
            addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Événement créé avec succès");
            refreshEventList();
            resetCurrentEvent();
            return "events?faces-redirect=true";
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage());
            return null;
        }
    }

    public String updateEvent() {
        try {
            // S'assurer que la date/heure est bien définie
            updateEventDateTime();

            eventService.updateEvent(currentEvent);
            addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Événement mis à jour avec succès");
            refreshEventList();
            return "events?faces-redirect=true";
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

    // ACTIONS DE NAVIGATION
    public String editEvent() {
        try {
            // S'assurer que la date/heure est bien définie
            updateEventDateTime();

            eventService.updateEvent(currentEvent);
            addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Événement modifié avec succès");
            refreshEventList();
            return "events?faces-redirect=true";
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage());
            return null;
        }
    }

    public String editEvent(Long eventId) {
        try {
            currentEvent = eventService.findById(eventId).orElse(null);
            if (currentEvent == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Événement non trouvé");
                return null;
            }

            // Initialiser les champs temporaires avec les valeurs de l'événement - CORRIGÉ
            initializeDateTimeFields();

            return "event-form?faces-redirect=true";
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage());
            return null;
        }
    }

    public void loadEventForEdit(Long eventId) {
        try {
            currentEvent = eventService.findById(eventId).orElse(null);
            if (currentEvent == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Événement non trouvé");
            } else {
                // Initialiser les valeurs temporaires à partir de l'événement chargé
                initializeDateTimeFields();
            }
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage());
        }
    }

    // NOUVELLE MÉTHODE pour initialiser les champs date/heure
    private void initializeDateTimeFields() {
        if (currentEvent != null && currentEvent.getEventDate() != null) {
            tempDate = currentEvent.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            tempTime = currentEvent.getEventDate().format(DateTimeFormatter.ofPattern("HH:mm"));
            System.out.println("Champs initialisés - Date: " + tempDate + ", Heure: " + tempTime);
        } else {
            tempDate = null;
            tempTime = null;
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

    // ACTIONS DE GESTION DES TICKETS
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

    // ACTIONS DE RECHERCHE ET FILTRAGE
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

    // MÉTHODES UTILITAIRES PRIVÉES
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

        // Initialiser avec une date par défaut (demain à 10h00)
        LocalDateTime defaultDateTime = LocalDateTime.now().plusDays(1)
                .withHour(10).withMinute(0).withSecond(0).withNano(0);
        currentEvent.setEventDate(defaultDateTime);

        // Initialiser les valeurs temporaires
        tempDate = defaultDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        tempTime = defaultDateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
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

    // GETTERS ET SETTERS
    public Event getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(Event currentEvent) {
        this.currentEvent = currentEvent;
        // Synchroniser les champs date/heure lors du set
        initializeDateTimeFields();
    }

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

    public String getSearchTitle() {
        return searchTitle;
    }

    public void setSearchTitle(String searchTitle) {
        this.searchTitle = searchTitle;
    }

    public String getSearchCity() {
        return searchCity;
    }

    public void setSearchCity(String searchCity) {
        this.searchCity = searchCity;
    }

    public Event.EventCategory getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(Event.EventCategory selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public Event.EventStatus getSelectedStatus() {
        return selectedStatus;
    }

    public void setSelectedStatus(Event.EventStatus selectedStatus) {
        this.selectedStatus = selectedStatus;
    }

    public int getTicketsToSell() {
        return ticketsToSell;
    }

    public void setTicketsToSell(int ticketsToSell) {
        this.ticketsToSell = ticketsToSell;
    }

    public EventService.EventStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(EventService.EventStatistics statistics) {
        this.statistics = statistics;
    }

    // MÉTHODES POUR LES SELECT ITEMS DANS LES FORMULAIRES JSF
    public List<Event.EventCategory> getEventCategories() {
        return Arrays.asList(Event.EventCategory.values());
    }

    public List<Event.EventStatus> getEventStatuses() {
        return Arrays.asList(Event.EventStatus.values());
    }

    // MÉTHODES UTILITAIRES POUR LES VUES
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
            case ACTIVE:
                return "badge-success";
            case UPCOMING:
                return "badge-info";
            case COMPLETED:
                return "badge-secondary";
            case CANCELLED:
                return "badge-danger";
            default:
                return "badge-light";
        }
    }

    public String getCategoryClass(Event.EventCategory category) {
        switch (category) {
            case BUSINESS:
                return "category-business";
            case ENTERTAINMENT:
                return "category-entertainment";
            case EDUCATION:
                return "category-education";
            case SPORT:
                return "category-sport";
            case SOCIAL:
                return "category-social";
            case TECHNOLOGY:
                return "category-technology";
            default:
                return "category-default";
        }
    }

    public String formatEventDate(Event event) {
        if (event == null || event.getEventDate() == null) {
            return "Date non définie";
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
            return event.getEventDate().format(formatter);
        } catch (Exception e) {
            return "Date invalide";
        }
    }
}