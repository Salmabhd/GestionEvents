package com.example.projetsdr.controller;


import com.example.projetsdr.model.Admin;
import com.example.projetsdr.repository.AdminRepository;
import com.example.projetsdr.service.AdminService;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import javax.sql.DataSource;
import java.io.Serializable;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private String email;
    private String password;
    private boolean loggedIn;

    @Resource(lookup = "java:/MySqldms_db")
    private DataSource dataSource;

    public String login() {
        AdminRepository repo = new AdminRepository(dataSource);
        AdminService service = new AdminService(repo);
        Admin admin = service.login(email, password);

        if (admin != null) {
            loggedIn = true;
            password = null;
            addMessage("Connexion réussie pour " + admin.getEmail(), FacesMessage.SEVERITY_INFO);
            return "welcome?faces-redirect=true";
        } else {
            addMessage("Email ou mot de passe incorrect", FacesMessage.SEVERITY_ERROR);
            return null;
        }
    }

    public String logout() {
        loggedIn = false;
        email = null;
        password = null;
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        addMessage("Déconnexion réussie", FacesMessage.SEVERITY_INFO);
        return "login?faces-redirect=true";
    }

    private void addMessage(String msg, FacesMessage.Severity type) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(type, msg, null));
    }

    // Getters / Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isLoggedIn() { return loggedIn; }
}
