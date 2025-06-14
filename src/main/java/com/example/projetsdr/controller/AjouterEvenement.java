package com.example.projetsdr.controller;

import com.example.projetsdr.model.Event;
import com.example.projetsdr.repository.EventAddRepository;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("eventBean")
@SessionScoped
public class AjouterEvenement implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AjouterEvenement.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER_SLASH = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_FORMATTER_DASH = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // Champs du formulaire adaptés au nouveau modèle
    private String title;
    private String description;
    private String date;
    private String heure;
    private String venue;
    private String city;
    private String category;
    private String maxParticipants;
    private String ticketPrice;

    @Resource(lookup = "java:/MySqldms_db")
    private DataSource dataSource;

    public String ajouterEvenement() {
        try {
            // Test de la connexion à la base de données
            if (!testDatabaseConnection()) {
                addErrorMessage("Impossible de se connecter à la base de données");
                return null;
            }

            validateFields();
            Event event = createEventFromForm();

            LOGGER.info("Tentative de sauvegarde de l'événement: " + event.getTitle());

            EventAddRepository repo = new EventAddRepository(dataSource);
            if (repo.save(event)) {
                addSuccessMessage("Événement créé avec succès: " + event.getTitle());
                clearFields();
                return null; // Rester sur la même page
            } else {
                addErrorMessage("Échec de la sauvegarde de l'événement - Vérifiez les logs pour plus de détails");
                return null;
            }
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

    private boolean testDatabaseConnection() {
        try {
            if (dataSource == null) {
                LOGGER.severe("DataSource est null - vérifiez la configuration JNDI");
                return false;
            }

            try (Connection conn = dataSource.getConnection()) {
                LOGGER.info("Connexion à la base de données réussie");
                return conn != null && !conn.isClosed();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur de connexion à la base de données", e);
            return false;
        }
    }

    private Event createEventFromForm() {
        LocalDateTime eventDateTime = parseDateTime(date, heure);
        int maxPart = parseMaxParticipants(maxParticipants);
        Double price = parseTicketPrice(ticketPrice);

        Event event = new Event();
        event.setTitle(title.trim());
        event.setDescription(description != null ? description.trim() : "");
        event.setEventDate(eventDateTime);
        event.setVenue(venue.trim());
        event.setCity(city != null ? city.trim() : "");
        event.setCategory(category != null ? category.trim() : "");
        event.setStatus("ACTIVE");
        event.setMaxParticipants(maxPart);
        event.setTicketsSold(0);
        event.setTicketPrice(price);

        return event;
    }

    private void validateFields() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre est obligatoire");
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
        if (maxParticipants == null || maxParticipants.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nombre maximum de participants est obligatoire");
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

            if (dateTime.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("La date/heure ne peut pas être dans le passé");
            }
            return dateTime;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Format de date/heure invalide. Utilisez JJ/MM/AAAA ou JJ-MM-AAAA pour la date et HH:mm pour l'heure");
        }
    }

    private int parseMaxParticipants(String maxParticipantsStr) {
        try {
            int nombre = Integer.parseInt(maxParticipantsStr.trim());
            if (nombre <= 0) {
                throw new IllegalArgumentException("Le nombre maximum de participants doit être positif");
            }
            if (nombre > 100000) {
                throw new IllegalArgumentException("Maximum 100000 participants autorisés");
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
            double price = Double.parseDouble(ticketPriceStr.trim());
            if (price < 0) {
                throw new IllegalArgumentException("Le prix du billet ne peut pas être négatif");
            }
            return price;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Le prix du billet doit être un nombre valide");
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
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    // Getters/Setters adaptés au nouveau modèle
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