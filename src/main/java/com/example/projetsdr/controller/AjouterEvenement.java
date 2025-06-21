package com.example.projetsdr.controller;

import com.example.projetsdr.model.Event;
import com.example.projetsdr.service.EventAddService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
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

    @Inject
    private EventAddService eventService;

    public String ajouterEvenement() {
        try {
            // Log pour debug
            LOGGER.info("=== Début création événement ===");
            LOGGER.info("Title: " + title);
            LOGGER.info("Date: " + date);
            LOGGER.info("Heure: " + heure);
            LOGGER.info("Venue: " + venue);
            LOGGER.info("City: " + city);
            LOGGER.info("Category: " + category);
            LOGGER.info("MaxParticipants: " + maxParticipants);
            LOGGER.info("TicketPrice: " + ticketPrice);
            LOGGER.info("Description: " + (description != null ? description.substring(0, Math.min(50, description.length())) + "..." : "null"));

            // Validation personnalisée (JSF required est retiré pour éviter les conflits)
            validateFields();

            Event event = createEventFromForm();

            LOGGER.info("Event créé en mémoire: " + event.getTitle());

            // Vérifier que le service est injecté
            if (eventService == null) {
                throw new RuntimeException("EventAddService n'est pas injecté correctement");
            }

            Event savedEvent = eventService.createEvent(event);
            LOGGER.info("Event sauvegardé avec ID: " + (savedEvent.getId() != null ? savedEvent.getId() : "null"));

            addSuccessMessage("Événement créé avec succès: " + savedEvent.getTitle());
            clearFields();
            return null; // Rester sur la même page

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

    private Event createEventFromForm() {
        LocalDateTime eventDateTime = parseDateTime(date, heure);
        Integer maxPart = parseMaxParticipants(maxParticipants);
        Double price = parseTicketPrice(ticketPrice);
        String eventCategory = parseCategory(category);

        Event event = new Event();
        event.setTitle(title.trim());
        event.setDescription(description != null ? description.trim() : "");
        event.setEventDate(eventDateTime);
        event.setVenue(venue.trim());
        event.setCity(city != null ? city.trim() : "");
        event.setCategory(eventCategory);
        event.setStatus("PLANNED");
        event.setMaxParticipants(maxPart);
        event.setTicketsSold(0);
        event.setTicketPrice(price);

        // Définir les timestamps
        LocalDateTime now = LocalDateTime.now();
        event.setCreatedAt(now);
        event.setUpdatedAt(now);

        return event;
    }

    private void validateFields() {
        // Validation du titre
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre est obligatoire");
        }
        if (title.trim().length() > 200) {
            throw new IllegalArgumentException("Le titre ne peut pas dépasser 200 caractères");
        }

        // Validation de la date
        if (date == null || date.trim().isEmpty()) {
            throw new IllegalArgumentException("La date est obligatoire");
        }

        // Validation de l'heure
        if (heure == null || heure.trim().isEmpty()) {
            throw new IllegalArgumentException("L'heure est obligatoire");
        }

        // Validation du lieu
        if (venue == null || venue.trim().isEmpty()) {
            throw new IllegalArgumentException("Le lieu est obligatoire");
        }
        if (venue.trim().length() > 300) {
            throw new IllegalArgumentException("Le lieu ne peut pas dépasser 300 caractères");
        }

        // Validation de la ville
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("La ville est obligatoire");
        }
        if (city.trim().length() > 100) {
            throw new IllegalArgumentException("La ville ne peut pas dépasser 100 caractères");
        }

        // Validation du nombre de participants
        if (maxParticipants == null || maxParticipants.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nombre maximum de participants est obligatoire");
        }

        // Validation de la description
        if (description != null && description.trim().length() > 5000) {
            throw new IllegalArgumentException("La description ne peut pas dépasser 5000 caractères");
        }
    }

    private LocalDateTime parseDateTime(String dateStr, String timeStr) {
        try {
            LocalDate datePart;
            String cleanDateStr = dateStr.trim();

            // Essayer d'abord avec le format slash, puis avec le format tiret
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

            // Vérifier que la date n'est pas dans le passé (avec une marge de 1 heure)
            if (dateTime.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new IllegalArgumentException("La date/heure doit être au moins 1 heure dans le futur");
            }

            // Vérifier que la date n'est pas trop loin dans le futur (5 ans)
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
            return null; // Prix optionnel
        }

        try {
            // Remplacer les virgules par des points pour la compatibilité
            String cleanPrice = ticketPriceStr.trim().replace(",", ".");
            double price = Double.parseDouble(cleanPrice);
            if (price < 0) {
                throw new IllegalArgumentException("Le prix du billet ne peut pas être négatif");
            }
            if (price > 10000) {
                throw new IllegalArgumentException("Le prix du billet ne peut pas dépasser 10 000€");
            }
            // Arrondir à 2 décimales
            return Math.round(price * 100.0) / 100.0;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Le prix du billet doit être un nombre valide (ex: 25.50)");
        }
    }

    private String parseCategory(String categoryStr) {
        if (categoryStr == null || categoryStr.trim().isEmpty()) {
            return "SOCIAL";
        }

        // Amélioration du mapping des catégories
        switch (categoryStr.toLowerCase().trim()) {
            case "technology":
                return "TECHNOLOGY";
            case "theatre":
            case "concert":
            case "cinema":
                return "ENTERTAINMENT";
            case "conference":
                return "BUSINESS";
            case "sport":
                return "SPORT";
            case "education":
                return "EDUCATION";
            case "art":
                return "ART"; // Maintenant mappé correctement
            case "party":
            case "food":
            case "other":
            default:
                return "SOCIAL";
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
}