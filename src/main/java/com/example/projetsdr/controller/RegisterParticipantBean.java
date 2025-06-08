package com.example.projetsdr.controller;

import com.example.projetsdr.model.Participant;
import com.example.projetsdr.repository.ParticipantRegisterRepository;
import com.example.projetsdr.service.participantRegisterService;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import javax.sql.DataSource;
import java.io.Serializable;

@Named("registerParticipantBean")
@RequestScoped
public class RegisterParticipantBean implements Serializable {

    private String nom;
    private String email;
    private String password;
    private String confirmPassword;

    @Resource(lookup = "java:/MySqldms_db")
    private DataSource dataSource;

    public String register() {
        // Vérifier si les mots de passe correspondent
        if (!password.equals(confirmPassword)) {
            addMessage("Les mots de passe ne correspondent pas", FacesMessage.SEVERITY_ERROR);
            return null;
        }

        // Créer un nouveau participant
        Participant participant = new Participant();
        participant.setNom(nom);
        participant.setEmail(email);
        participant.setPassword(password);

        // Sauvegarder le participant
        ParticipantRegisterRepository repo = new ParticipantRegisterRepository(dataSource);
        participantRegisterService service = new participantRegisterService(repo);

        boolean success = service.createParticipant(participant);

        if (success) {
            addMessage("Inscription réussie pour " + nom, FacesMessage.SEVERITY_INFO);
            // Réinitialiser les champs
            clearFields();
            return "participant-login?faces-redirect=true";
        } else {
            addMessage("Erreur lors de l'inscription. Veuillez réessayer.", FacesMessage.SEVERITY_ERROR);
            return null;
        }
    }

    private void clearFields() {
        nom = null;
        email = null;
        password = null;
        confirmPassword = null;
    }

    private void addMessage(String msg, FacesMessage.Severity type) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(type, msg, null));
    }

    // Getters / Setters
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}