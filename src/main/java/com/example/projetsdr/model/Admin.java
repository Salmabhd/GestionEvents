package com.example.projetsdr.model;

public class Admin {
    private int id;
    private String email;
    private String password;

    // Constructeur vide (recommandé pour les frameworks comme JPA)
    public Admin() {
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
