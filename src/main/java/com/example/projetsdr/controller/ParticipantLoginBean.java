package com.example.projetsdr.controller;

import com.example.projetsdr.model.Participant;
import com.example.projetsdr.repository.ParticipantRepository;
import com.example.projetsdr.service.ParticipantService;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

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
        ParticipantRepository repo = new ParticipantRepository(dataSource);
        ParticipantService service = new ParticipantService(repo);
        Participant participant = service.login(email, password);

        if (participant != null) {
            loggedIn = true;
            currentParticipant = participant;
            password = null;
            addMessage("Connexion réussie pour " + participant.getEmail(), FacesMessage.SEVERITY_INFO);
            return "participant-dashboard?faces-redirect=true";
        } else {
            addMessage("Email ou mot de passe incorrect", FacesMessage.SEVERITY_ERROR);
            return null;
        }
    }

    public String logout() {
        loggedIn = false;
        email = null;
        password = null;
        currentParticipant = null;
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        addMessage("Déconnexion réussie", FacesMessage.SEVERITY_INFO);
        return "participant-login?faces-redirect=true";
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

    public Participant getCurrentParticipant() {
        return currentParticipant;
    }

    public void setCurrentParticipant(Participant currentParticipant) {
        this.currentParticipant = currentParticipant;
    }
}