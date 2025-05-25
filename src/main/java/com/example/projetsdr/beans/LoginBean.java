package com.example.projetsdr.beans;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Named;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private String email;
    private String password;
    private boolean loggedIn = false;

    @Resource(lookup = "java:/MySqldms_db")
    private DataSource dataSource;

    // Méthode pour tester la connexion à la base de données
    public void testConnection() {
        System.out.println("=== TEST CONNEXION DB ===");

        if (dataSource == null) {
            System.err.println("DataSource est null !");
            addErrorMessage("DataSource non configuré");
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Connexion DB réussie !");
                addSuccessMessage("Connexion à la base de données réussie !");

                // Test de la table admins
                String testSql = "SELECT COUNT(*) as count FROM admins";
                try (PreparedStatement ps = conn.prepareStatement(testSql);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt("count");
                        System.out.println("Nombre d'admins dans la DB: " + count);
                        addSuccessMessage("Table 'admins' trouvée avec " + count + " enregistrement(s)");
                    }
                }
            } else {
                System.err.println("✗ Connexion DB fermée ou invalide");
                addErrorMessage("Connexion à la base de données invalide");
            }
        } catch (Exception e) {
            System.err.println("✗ Erreur connexion DB: " + e.getMessage());
            e.printStackTrace();
            addErrorMessage("Erreur de connexion DB: " + e.getMessage());
        }
    }

    public String login() {
        System.out.println("=== DEBUT LOGIN ===");
        System.out.println("Email: " + email);
        System.out.println("Password: " + (password != null ? "[FOURNI]" : "[VIDE]"));

        // Validation basique
        if (email == null || email.trim().isEmpty()) {
            addErrorMessage("L'email est obligatoire");
            return null;
        }

        if (password == null || password.trim().isEmpty()) {
            addErrorMessage("Le mot de passe est obligatoire");
            return null;
        }

        if (dataSource == null) {
            System.err.println("DataSource est null !");
            addErrorMessage("Configuration de base de données manquante");
            return null;
        }

        try (Connection conn = dataSource.getConnection()) {
            System.out.println("Connexion DB: " + (conn != null ? "OK" : "ÉCHEC"));

            String sql = "SELECT id, email FROM admins WHERE email = ? AND password = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email.trim());
            ps.setString(2, password);

            System.out.println("Exécution de la requête SQL...");
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("LOGIN RÉUSSI - Redirection vers welcome.xhtml");
                loggedIn = true;

                // Nettoyer les champs de mot de passe pour la sécurité
                password = null;

                addSuccessMessage("Connexion réussie ! Bienvenue " + rs.getString("email"));

                // Redirection vers welcome
                return "welcome?faces-redirect=true";

            } else {
                System.out.println("LOGIN ÉCHEC - Utilisateur non trouvé");
                addErrorMessage("Email ou mot de passe incorrect");
                return null;
            }

        } catch (Exception e) {
            System.err.println("ERREUR LOGIN: " + e.getMessage());
            e.printStackTrace();
            addErrorMessage("Erreur système: " + e.getMessage());
            return null;
        }
    }

    // Validation de l'email
    public void validateEmail(FacesContext context, jakarta.faces.component.UIComponent component, Object value)
            throws ValidatorException {
        String emailValue = (String) value;
        if (emailValue != null && !emailValue.contains("@")) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Format d'email invalide", null)
            );
        }
    }

    // Méthode de déconnexion
    public String logout() {
        System.out.println("=== LOGOUT ===");
        loggedIn = false;
        email = null;
        password = null;
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        addSuccessMessage("Vous avez été déconnecté avec succès");
        return "login?faces-redirect=true";
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    private void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    // Getters et Setters
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
}