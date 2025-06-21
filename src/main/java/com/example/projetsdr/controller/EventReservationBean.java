package com.example.projetsdr.controller;

import com.example.projetsdr.model.EventEntity;
import com.example.projetsdr.model.EventParticipation;
import com.example.projetsdr.service.EventParticipationService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.Serializable;

@Named
@SessionScoped
public class EventReservationBean implements Serializable {

    private EventParticipation participation = new EventParticipation();
    private EventEntity selectedEvent;

    @Inject
    private EventParticipationService participationService;

    public void initReservation(EventEntity event) {
        this.selectedEvent = event;
        this.participation = new EventParticipation();
        this.participation.setEvent(event);

        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("reservation.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String enregistrerReservation() {
        if (participationService.register(participation)) {
            return "confirmation.xhtml?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erreur", "Cette adresse e-mail est déjà enregistrée pour cet événement."));
            return null;
        }
    }

    public EventParticipation getParticipation() { return participation; }
    public EventEntity getSelectedEvent() { return selectedEvent; }
}
