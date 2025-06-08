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
import java.sql.Connection;

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

        // Vérifier les champs obligatoires
        if (nom == null || nom.trim().isEmpty()) {
            addMessage("Le nom est obligatoire", FacesMessage.SEVERITY_ERROR);
            return null;
        }

        if (email == null || email.trim().isEmpty()) {
            addMessage("L'email est obligatoire", FacesMessage.SEVERITY_ERROR);
            return null;
        }

        if (password == null || password.trim().isEmpty()) {
            addMessage("Le mot de passe est obligatoire", FacesMessage.SEVERITY_ERROR);
            return null;
        }

        try {
            // Test de la connexion à la base de données
            if (dataSource == null) {
                addMessage("Erreur de configuration de la base de données", FacesMessage.SEVERITY_ERROR);
                return null;
            }

            // Créer un nouveau participant
            Participant participant = new Participant();
            participant.setNom(nom.trim());
            participant.setEmail(email.trim());
            participant.setPassword(password);

            System.out.println("Tentative d'inscription pour: " + nom + " - " + email);

            // Sauvegarder le participant
            ParticipantRegisterRepository repo = new ParticipantRegisterRepository(dataSource);
            participantRegisterService service = new participantRegisterService(repo);

            boolean success = service.createParticipant(participant);

            System.out.println("Résultat de l'inscription: " + success);

            if (success) {
                // SUCCÈS : Inscription réussie
                addMessage("Inscription réussie pour " + nom, FacesMessage.SEVERITY_INFO);
                clearFields();

                // CORRECTION : Navigation JSF simple et efficace
                return "/pages/Participant-login?faces-redirect=true";

            } else {
                // ÉCHEC : Erreur lors de l'inscription
                addMessage("Erreur lors de l'inscription. Veuillez réessayer.", FacesMessage.SEVERITY_ERROR);
                return null;
            }

        } catch (Exception e) {
            System.err.println("Exception lors de l'inscription: " + e.getMessage());
            e.printStackTrace();
            addMessage("Erreur technique: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
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