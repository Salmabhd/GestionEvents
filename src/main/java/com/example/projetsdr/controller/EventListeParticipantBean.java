package com.example.projetsdr.controller;

import com.example.projetsdr.model.EventParticipation;
import com.example.projetsdr.repository.EventListeParticipantRepository;
import com.example.projetsdr.service.EventListeParticipationService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import javax.sql.DataSource;
import java.io.Serializable;
import java.util.List;

@Named("eventListeParticipantBean")
@SessionScoped
public class EventListeParticipantBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:/MySqldms_db")
    private DataSource dataSource;

    private EventListeParticipationService eventParticipationService;
    private List<EventParticipation> participations;

    // Filtres
    private Long eventId;
    private String filterEmail;
    private String filterStatus;

    @PostConstruct
    public void init() {
        EventListeParticipantRepository repository = new EventListeParticipantRepository(dataSource);
        eventParticipationService = new EventListeParticipationService(repository);
        loadParticipations();
    }

    public void loadParticipations() {
        try {
            participations = eventParticipationService.getAllParticipations();
            addMessage("Participants chargés avec succès", FacesMessage.SEVERITY_INFO);
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

            boolean deleted = eventParticipationService.deleteParticipant(participantId);

            if (deleted) {
                addMessage("Participant supprimé avec succès", FacesMessage.SEVERITY_INFO);
                // Recharger la liste après suppression
                loadParticipations();
            } else {
                addMessage("Participant introuvable ou déjà supprimé", FacesMessage.SEVERITY_WARN);
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
            if (!hasParticipants()) {
                addMessage("Aucun participant à supprimer", FacesMessage.SEVERITY_WARN);
                return;
            }

            int deletedCount = eventParticipationService.deleteAllParticipants();

            if (deletedCount > 0) {
                addMessage("Tous les participants ont été supprimés (" + deletedCount + " suppression(s))",
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
     * Vérifie s'il y a des participants
     */
    public boolean hasParticipants() {
        return participations != null && !participations.isEmpty();
    }

    /**
     * Obtient le nombre total de participants
     */
    public long getTotalParticipantsCount() {
        return eventParticipationService.getTotalParticipantsCount();
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