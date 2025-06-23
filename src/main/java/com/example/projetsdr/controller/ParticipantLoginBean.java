package com.example.projetsdr.controller;

import com.example.projetsdr.model.Participant;
import com.example.projetsdr.repository.ParticipantRepository;
import com.example.projetsdr.service.ParticipantService;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.Serializable;

@Named("participantLoginBean")
@SessionScoped
public class ParticipantLoginBean implements Serializable {

    private String email;
    private String password;
    private boolean loggedIn;
    private Participant currentParticipant;

    @Resource(lookup = "java:/MySqldms_db")
    private DataSource dataSource;

    public String login() {
        try {
            ParticipantRepository repo = new ParticipantRepository(dataSource);
            ParticipantService service = new ParticipantService(repo);

            // Vérification des champs
            if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                addMessage("Veuillez remplir tous les champs", FacesMessage.SEVERITY_ERROR);
                return null;
            }

            Participant participant = service.login(email.trim(), password);

            if (participant != null) {
                // Connexion réussie
                loggedIn = true;
                currentParticipant = participant;
                password = null; // Sécurité : nettoyer le mot de passe

                // Stocker l'utilisateur dans la session HTTP pour EventDisplayBean
                HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                        .getExternalContext().getSession(true);
                session.setAttribute("loggedUser", participant);

                addMessage("Connexion réussie pour " + participant.getEmail(), FacesMessage.SEVERITY_INFO);

                // Vérifier s'il y a une réservation en attente ET un flag indiquant qu'on vient d'une tentative de réservation
                Long eventToReserve = (Long) session.getAttribute("eventToReserve");
                Boolean fromReservationAttempt = (Boolean) session.getAttribute("fromReservationAttempt");

                if (eventToReserve != null && fromReservationAttempt != null && fromReservationAttempt) {
                    // Il y a un événement à réserver ET on vient bien d'une tentative de réservation
                    System.out.println("Redirection vers réservation après login pour événement ID : " + eventToReserve);

                    // Nettoyer le flag après utilisation
                    session.removeAttribute("fromReservationAttempt");

                    return "reservation?faces-redirect=true";
                }

                // Connexion normale, pas de réservation en attente OU connexion directe
                return "accueil?faces-redirect=true";

            } else {
                // Connexion échouée
                addMessage("Email ou mot de passe incorrect", FacesMessage.SEVERITY_ERROR);
                return null;
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la connexion : " + e.getMessage());
            e.printStackTrace();
            addMessage("Erreur lors de la connexion. Veuillez réessayer.", FacesMessage.SEVERITY_ERROR);
            return null;
        }
    }

    public String logout() {
        loggedIn = false;
        email = null;
        password = null;
        currentParticipant = null;

        // Invalider complètement la session
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

        addMessage("Déconnexion réussie", FacesMessage.SEVERITY_INFO);
        return null;
    }

    /**
     * Méthode utilitaire pour vérifier si l'utilisateur est connecté
     * Utilisée par d'autres beans
     */
    public boolean isUserLoggedIn() {
        return loggedIn && currentParticipant != null;
    }

    /**
     * Méthode pour obtenir l'utilisateur connecté depuis la session HTTP
     * Utilisée par EventDisplayBean
     */
    public static Participant getLoggedUserFromSession() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
            if (session != null) {
                return (Participant) session.getAttribute("loggedUser");
            }
        }
        return null;
    }

    private void addMessage(String msg, FacesMessage.Severity type) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(type, msg, null));
    }

    // Getters / Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public Participant getCurrentParticipant() {
        return currentParticipant;
    }

    public void setCurrentParticipant(Participant currentParticipant) {
        this.currentParticipant = currentParticipant;
    }
}