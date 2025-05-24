package com.example.eventsmanagement.controller;

import com.example.eventsmanagement.model.Admin;
import com.example.eventsmanagement.repository.AdminRepository;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

@Named
@ViewScoped
public class LoginController implements Serializable {

    private String email;
    private String password;
    private String username;

    @Autowired
    private AdminRepository adminRepository;

    public String login() {
        Admin admin = adminRepository.findByEmailAndPasswordAndUsername(email, password, username);

        if (admin != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("admin", admin);
            return "/admins.xhtml?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Identifiants incorrects", null));
            return null;
        }
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }

    // Getters et Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}