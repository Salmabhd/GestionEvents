package com.example.projetsdr.model;

public class Participant {
    private int id;
    private String nom;
    private String email;
    private String password;

    // Constructeur vide (recommandé pour les frameworks comme JPA)
    public Participant() {
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
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

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}