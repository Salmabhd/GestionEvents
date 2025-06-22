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
     * Méthode alternative pour supprimer via void (pour AJAX)
     */
    public void deleteParticipantAjax(Long participantId) {
        deleteParticipant(participantId);
    }

    // Méthode utilitaire
    private void addMessage(String summary, FacesMessage.Severity severity) {
        FacesMessage message = new FacesMessage(severity, summary, null);
        FacesContext.getCurrentInstance().addMessage(null, message);
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
}