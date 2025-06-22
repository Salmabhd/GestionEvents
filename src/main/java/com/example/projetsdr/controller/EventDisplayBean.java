package com.example.projetsdr.controller;

import com.example.projetsdr.model.EventEntity;
import com.example.projetsdr.model.Participant;
import com.example.projetsdr.service.EventDisplayService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class EventDisplayBean implements Serializable {

    private List<EventEntity> events;

    @Inject
    private EventDisplayService eventService;

    @Inject
    private EventReservationBean eventReservationBean;

    // AJOUT : Injection du ParticipantLoginBean
    @Inject
    private ParticipantLoginBean participantLoginBean;

    @PostConstruct
    public void init() {
        events = eventService.getAllEvents();
    }

    public List<EventEntity> getEvents() {
        return events;
    }

    /**
     * Méthode de réservation avec vérification de connexion
     */
    public void reserver(EventEntity event) {
        System.out.println("Tentative de réservation pour : " + event.getTitle());

        // Vérifier si l'utilisateur est connecté
        if (!isUserLoggedIn()) {
            // Stocker l'événement à réserver dans la session
            storeEventForReservation(event);

            // Rediriger vers la page de login
            redirectToLogin();
            return;
        }

        // L'utilisateur est connecté, procéder à la réservation
        System.out.println("Utilisateur connecté - Réservation : " + event.getTitle());
        eventReservationBean.initReservation(event);

        // Rediriger vers le formulaire de réservation
        redirectToReservation();
    }

    /**
     * Vérifie si l'utilisateur est connecté
     */
    private boolean isUserLoggedIn() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);

        if (session == null) {
            return false;
        }

        // Vérifier la présence de l'utilisateur connecté
        Participant loggedUser = (Participant) session.getAttribute("loggedUser");
        return loggedUser != null;
    }

    /**
     * Stocke l'événement à réserver dans la session
     */
    private void storeEventForReservation(EventEntity event) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);

        session.setAttribute("eventToReserve", event.getId());
        session.setAttribute("eventToReserveObject", event);
        // Flag pour indiquer qu'on vient d'une tentative de réservation
        session.setAttribute("fromReservationAttempt", true);

        System.out.println("Événement stocké pour réservation après login : " + event.getTitle());
    }

    /**
     * Redirige vers la page de login
     */
    private void redirectToLogin() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();

            // Rediriger vers la page de login
            externalContext.redirect("Participant-login.xhtml");
            facesContext.responseComplete();

        } catch (IOException e) {
            System.err.println("Erreur lors de la redirection vers le login : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Redirige vers le formulaire de réservation
     */
    private void redirectToReservation() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();

            // Rediriger vers la page de réservation
            externalContext.redirect("reservation.xhtml");
            facesContext.responseComplete();

        } catch (IOException e) {
            System.err.println("Erreur lors de la redirection vers la réservation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Méthode utilitaire pour obtenir l'utilisateur connecté
     */
    public Participant getLoggedUser() {
        // MODIFICATION : Utiliser la méthode statique directement
        return ParticipantLoginBean.getLoggedUserFromSession();
    }
}