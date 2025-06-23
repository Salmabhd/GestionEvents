package com.example.projetsdr.controller;

import com.example.projetsdr.model.Event;
import com.example.projetsdr.service.EventAddService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("ajouterEvenementBean")
@SessionScoped
public class AjouterEvenement implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AjouterEvenement.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER_SLASH = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_FORMATTER_DASH = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // Champs du formulaire
    private String title;
    private String description;
    private String date;
    private String heure;
    private String venue;
    private String city;
    private String category;
    private String maxParticipants;
    private String ticketPrice;
    private Event.EventStatus status;

    @Inject
    private EventAddService eventService;

    // SOLUTION 1: Injecter EventBean pour rafraîchir sa liste
    @Inject
    private EventBean eventBean;

    public Event.EventStatus[] getStatuses() {
        return Event.EventStatus.values();
    }

    public Event.EventCategory[] getCategories() {
        return Event.EventCategory.values();
    }

    @Transactional
    public String ajouterEvenement() {
        try {
            LOGGER.info("=== Début création événement ===");
            validateFields();
            Event event = createEventFromForm();

            if (eventService == null) {
                throw new RuntimeException("EventAddService n'est pas injecté correctement");
            }

            Event savedEvent = eventService.createEvent(event);
            LOGGER.info("Event sauvegardé avec ID: " + (savedEvent.getId() != null ? savedEvent.getId() : "null"));

            // Force l'actualisation du cache
            eventService.refreshCache();

            // SOLUTION 1: Forcer le rafraîchissement de la liste dans EventBean
            if (eventBean != null) {
                eventBean.init(); // Rafraîchit la liste des événements
                LOGGER.info("Liste des événements rafraîchie dans EventBean");
            }

            addSuccessMessage("Événement créé avec succès : " + savedEvent.getTitle());
            clearFields();

            return "events.xhtml?faces-redirect=true";

        } catch (IllegalArgumentException e) {
            LOGGER.warning("Erreur de validation: " + e.getMessage());
            addErrorMessage(e.getMessage());
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur technique lors de la création de l'événement", e);
            addErrorMessage("Erreur technique: " + e.getMessage());
            return null;
        }
    }

    public String ajouterEvenementSansRedirection() {
        try {
            LOGGER.info("=== Début création événement ===");
            validateFields();
            Event event = createEventFromForm();

            if (eventService == null) {
                throw new RuntimeException("EventAddService n'est pas injecté correctement");
            }

            Event savedEvent = eventService.createEvent(event);
            LOGGER.info("Event sauvegardé avec ID: " + (savedEvent.getId() != null ? savedEvent.getId() : "null"));

            eventService.refreshCache();

            // SOLUTION 1: Forcer le rafraîchissement
            if (eventBean != null) {
                eventBean.init();
                LOGGER.info("Liste des événements rafraîchie dans EventBean");
            }

            addSuccessMessage("Événement créé avec succès : " + savedEvent.getTitle());
            clearFields();

            FacesContext.getCurrentInstance().getPartialViewContext().setRenderAll(true);
            return null;

        } catch (IllegalArgumentException e) {
            LOGGER.warning("Erreur de validation: " + e.getMessage());
            addErrorMessage(e.getMessage());
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur technique lors de la création de l'événement", e);
            addErrorMessage("Erreur technique: " + e.getMessage());
            return null;
        }
    }

    // Rest of the methods remain the same...
    private Event createEventFromForm() {
        LocalDateTime eventDateTime = parseDateTime(date, heure);
        Integer maxPart = parseMaxParticipants(maxParticipants);
        Double price = parseTicketPrice(ticketPrice);
        Event.EventCategory eventCategory = parseCategory(category);

        Event event = new Event();
        event.setTitle(title.trim());
        event.setDescription(description != null ? description.trim() : "");
        event.setEventDate(eventDateTime);
        event.setVenue(venue.trim());
        event.setCity(city != null ? city.trim() : "");
        event.setCategory(eventCategory);
        event.setStatus(status != null ? status : Event.EventStatus.UPCOMING);
        event.setMaxParticipants(maxPart);
        event.setTicketsSold(0);
        event.setTicketPrice(price);

        LocalDateTime now = LocalDateTime.now();
        event.setCreatedAt(now);
        event.setUpdatedAt(now);

        return event;
    }

    private void validateFields() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre est obligatoire");
        }
        if (title.trim().length() > 200) {
            throw new IllegalArgumentException("Le titre ne peut pas dépasser 200 caractères");
        }
        if (date == null || date.trim().isEmpty()) {
            throw new IllegalArgumentException("La date est obligatoire");
        }
        if (heure == null || heure.trim().isEmpty()) {
            throw new IllegalArgumentException("L'heure est obligatoire");
        }
        if (venue == null || venue.trim().isEmpty()) {
            throw new IllegalArgumentException("Le lieu est obligatoire");
        }
        if (venue.trim().length() > 300) {
            throw new IllegalArgumentException("Le lieu ne peut pas dépasser 300 caractères");
        }
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("La ville est obligatoire");
        }
        if (city.trim().length() > 100) {
            throw new IllegalArgumentException("La ville ne peut pas dépasser 100 caractères");
        }
        if (maxParticipants == null || maxParticipants.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nombre maximum de participants est obligatoire");
        }
        if (description != null && description.trim().length() > 5000) {
            throw new IllegalArgumentException("La description ne peut pas dépasser 5000 caractères");
        }
    }

    private LocalDateTime parseDateTime(String dateStr, String timeStr) {
        try {
            LocalDate datePart;
            String cleanDateStr = dateStr.trim();

            try {
                datePart = LocalDate.parse(cleanDateStr, DATE_FORMATTER_SLASH);
            } catch (DateTimeParseException e1) {
                try {
                    datePart = LocalDate.parse(cleanDateStr, DATE_FORMATTER_DASH);
                } catch (DateTimeParseException e2) {
                    throw new IllegalArgumentException("Format de date invalide. Utilisez JJ/MM/AAAA ou JJ-MM-AAAA");
                }
            }

            LocalTime timePart = LocalTime.parse(timeStr.trim(), TIME_FORMATTER);
            LocalDateTime dateTime = LocalDateTime.of(datePart, timePart);

            if (dateTime.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new IllegalArgumentException("La date/heure doit être au moins 1 heure dans le futur");
            }

            if (dateTime.isAfter(LocalDateTime.now().plusYears(5))) {
                throw new IllegalArgumentException("La date ne peut pas être plus de 5 ans dans le futur");
            }

            return dateTime;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Format de date/heure invalide. Utilisez JJ/MM/AAAA ou JJ-MM-AAAA pour la date et HH:mm pour l'heure");
        }
    }

    private Integer parseMaxParticipants(String maxParticipantsStr) {
        try {
            int nombre = Integer.parseInt(maxParticipantsStr.trim());
            if (nombre <= 0) {
                throw new IllegalArgumentException("Le nombre maximum de participants doit être positif");
            }
            if (nombre > 100000) {
                throw new IllegalArgumentException("Maximum 100 000 participants autorisés");
            }
            return nombre;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Le nombre maximum de participants doit être un nombre entier valide");
        }
    }

    private Double parseTicketPrice(String ticketPriceStr) {
        if (ticketPriceStr == null || ticketPriceStr.trim().isEmpty()) {
            return null;
        }

        try {
            String cleanPrice = ticketPriceStr.trim().replace(",", ".");
            double price = Double.parseDouble(cleanPrice);
            if (price < 0) {
                throw new IllegalArgumentException("Le prix du billet ne peut pas être négatif");
            }
            if (price > 10000) {
                throw new IllegalArgumentException("Le prix du billet ne peut pas dépasser 10 000€");
            }
            return Math.round(price * 100.0) / 100.0;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Le prix du billet doit être un nombre valide (ex: 25.50)");
        }
    }

    private Event.EventCategory parseCategory(String categoryStr) {
        if (categoryStr == null || categoryStr.trim().isEmpty()) {
            return Event.EventCategory.SOCIAL;
        }
        try {
            return Event.EventCategory.valueOf(categoryStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Event.EventCategory.SOCIAL;
        }
    }

    private void clearFields() {
        title = null;
        description = null;
        date = null;
        heure = null;
        venue = null;
        city = null;
        category = null;
        maxParticipants = null;
        ticketPrice = null;
        status = null;
    }

    private void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", message));
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", message));
    }

    // Getters et Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getHeure() { return heure; }
    public void setHeure(String heure) { this.heure = heure; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(String maxParticipants) { this.maxParticipants = maxParticipants; }

    public String getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(String ticketPrice) { this.ticketPrice = ticketPrice; }

    public Event.EventStatus getStatus() { return status; }
    public void setStatus(Event.EventStatus status) { this.status = status; }
}