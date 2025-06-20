package com.example.projetsdr.model;
import java.time.LocalDateTime;

public class EventParticipation {
    private long id;
    private long eventId;
    private int participantId;
    private String participantEmail;
    private LocalDateTime registrationDate;
    private String status;
    private LocalDateTime createdAt;

    // Constructeur vide
    public EventParticipation() {
    }

    // Getters
    public long getId() {
        return id;
    }

    public long getEventId() {
        return eventId;
    }

    public int getParticipantId() {
        return participantId;
    }

    public String getParticipantEmail() {
        return participantEmail;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public void setParticipantEmail(String participantEmail) {
        this.participantEmail = participantEmail;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
