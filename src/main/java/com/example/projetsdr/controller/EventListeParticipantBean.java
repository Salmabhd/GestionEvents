package com.example.projetsdr.controller;

import com.example.projetsdr.model.EventParticipation;
import com.example.projetsdr.repository.EventListeParticipantRepository;
import com.example.projetsdr.service.EventListeParticipationService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("eventListeParticipantBean")
@SessionScoped
public class EventListeParticipantBean implements Serializable {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @Inject
    private EventListeParticipantRepository repository;

    private EventListeParticipationService eventParticipationService;
    private List<EventParticipation> participations;

    // Filtres
    private Long eventId;
    private String filterEmail;
    private String filterStatus;

    @PostConstruct
    public void init() {
        eventParticipationService = new EventListeParticipationService(repository);
        loadParticipations();
    }

    public void loadParticipations() {
        try {
            participations = eventParticipationService.getAllParticipations();
            addMessage("Participants chargés avec succès (" + participations.size() + " trouvé(s))",
                    FacesMessage.SEVERITY_INFO);
        } catch (Exception e) {
            addMessage("Erreur lors du chargement: " + e.getMessage(),
                    FacesMessage.SEVERITY_ERROR);
        }
    }

    public String filterParticipations() {
        try {
            participations = eventParticipationService.getFilteredParticipations(
                    eventId, filterEmail, filterStatus);
            addMessage("Filtrage réussi - " + participations.size() + " participant(s) trouvé(s)",
                    FacesMessage.SEVERITY_INFO);
            return "success";
        } catch (Exception e) {
            addMessage("Erreur de filtrage: " + e.getMessage(),
                    FacesMessage.SEVERITY_ERROR);
            return "error";
        }
    }

    public void resetFilters() {
        this.eventId = null;
        this.filterEmail = null;
        this.filterStatus = null;
        loadParticipations();
        addMessage("Filtres réinitialisés", FacesMessage.SEVERITY_INFO);
    }

    /**
     * Méthode pour supprimer un participant (reste sur la même page)
     */
    public void deleteParticipant(Long participantId) {
        try {
            if (participantId == null) {
                addMessage("ID du participant invalide", FacesMessage.SEVERITY_ERROR);
                return;
            }

            // Vérifier si le participant existe avant suppression
            if (!eventParticipationService.participantExists(participantId)) {
                addMessage("Participant introuvable avec l'ID: " + participantId,
                        FacesMessage.SEVERITY_WARN);
                return;
            }

            boolean deleted = eventParticipationService.deleteParticipant(participantId);

            if (deleted) {
                addMessage("Participant supprimé avec succès (ID: " + participantId + ")",
                        FacesMessage.SEVERITY_INFO);
                // Recharger la liste après suppression
                loadParticipations();
            } else {
                addMessage("Échec de la suppression du participant", FacesMessage.SEVERITY_WARN);
            }
        } catch (Exception e) {
            addMessage("Erreur lors de la suppression: " + e.getMessage(),
                    FacesMessage.SEVERITY_ERROR);
        }
    }

    /**
     * Méthode pour supprimer tous les participants
     */
    public void deleteAllParticipants() {
        try {
            if (!hasParticipantsInList()) {
                addMessage("Aucun participant à supprimer dans la liste affichée",
                        FacesMessage.SEVERITY_WARN);
                return;
            }

            long totalBeforeDelete = getTotalParticipantsCount();
            int deletedCount = eventParticipationService.deleteAllParticipants();

            if (deletedCount > 0) {
                addMessage("Suppression terminée: " + deletedCount + " participant(s) supprimé(s) de la base de données",
                        FacesMessage.SEVERITY_INFO);
                // Recharger la liste vide
                loadParticipations();
            } else {
                addMessage("Aucun participant n'a été supprimé", FacesMessage.SEVERITY_WARN);
            }
        } catch (Exception e) {
            addMessage("Erreur lors de la suppression de tous les participants: " + e.getMessage(),
                    FacesMessage.SEVERITY_ERROR);
        }
    }

    /**
     * Vérifie s'il y a des participants dans la liste affichée
     */
    public boolean hasParticipantsInList() {
        return participations != null && !participations.isEmpty();
    }

    /**
     * Vérifie s'il y a des participants en base de données
     */
    public boolean hasParticipants() {
        return eventParticipationService.hasParticipants();
    }

    /**
     * Obtient le nombre total de participants en base
     */
    public long getTotalParticipantsCount() {
        return eventParticipationService.getTotalParticipantsCount();
    }

    /**
     * Obtient le nombre de participants dans la liste affichée
     */
    public int getDisplayedParticipantsCount() {
        return participations != null ? participations.size() : 0;
    }

    /**
     * Méthode pour obtenir l'ID de l'événement d'un participant
     * Compatible avec la nouvelle entité
     */
    public Long getParticipantEventId(EventParticipation participation) {
        if (participation != null && participation.getEvent() != null) {
            return participation.getEvent().getId();
        }
        return null;
    }

    /**
     * Méthode pour obtenir le nom de l'événement d'un participant
     * FIXED: Utilise getTitle() au lieu de getName()
     */
    public String getParticipantEventName(EventParticipation participation) {
        if (participation != null && participation.getEvent() != null) {
            return participation.getEvent().getTitle(); // Changed from getName() to getTitle()
        }
        return "Événement inconnu";
    }

    /**
     * Méthode pour formater la date d'inscription
     */
    public String getFormattedRegistrationDate(EventParticipation participation) {
        if (participation != null && participation.getRegistrationDate() != null) {
            return participation.getRegistrationDate().toString();
        }
        return "Date inconnue";
    }

    /**
     * Méthode utilitaire pour ajouter des messages
     */
    private void addMessage(String message, FacesMessage.Severity severity) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            FacesMessage facesMessage = new FacesMessage(severity, message, null);
            context.addMessage(null, facesMessage);
        }
    }

    // Getters et Setters
    public List<EventParticipation> getParticipations() {
        return participations;
    }

    public void setParticipations(List<EventParticipation> participations) {
        this.participations = participations;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getFilterEmail() {
        return filterEmail;
    }

    public void setFilterEmail(String filterEmail) {
        this.filterEmail = filterEmail;
    }

    public String getFilterStatus() {
        return filterStatus;
    }

    public void setFilterStatus(String filterStatus) {
        this.filterStatus = filterStatus;
    }

    public EventListeParticipationService getEventParticipationService() {
        return eventParticipationService;
    }

    public void setEventParticipationService(EventListeParticipationService eventParticipationService) {
        this.eventParticipationService = eventParticipationService;
    }
}