package com.example.projetsdr.service;

import com.example.projetsdr.model.EventParticipation;
import com.example.projetsdr.repository.EventListeParticipantRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EventListeParticipationService {

    private final EventListeParticipantRepository repository;

    public EventListeParticipationService(EventListeParticipantRepository repository) {
        this.repository = repository;
    }

    public List<EventParticipation> getAllParticipations() {
        return repository.findAll();
    }

    public List<EventParticipation> getFilteredParticipations(Long eventId, String email, String status) {
        return repository.findWithFilters(eventId, email, status);
    }

    public boolean updateParticipation(EventParticipation participation) {
        try {
            repository.save(participation);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Method that matches what your controller is calling
    public boolean deleteParticipant(Long participantId) {
        if (participantId == null) {
            throw new IllegalArgumentException("ID du participant invalide: null");
        }
        try {
            return repository.deleteById(participantId.longValue());
        } catch (Exception e) {
            return false;
        }
    }

    // Keep the original method for backward compatibility
    public boolean deleteParticipation(long id) {
        try {
            return repository.deleteById(id);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Nouvelle méthode pour supprimer tous les participants
     */
    public int deleteAllParticipants() {
        try {
            int deletedCount = repository.deleteAll();
            return deletedCount;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression de tous les participants: " + e.getMessage(), e);
        }
    }

    /**
     * Méthode pour obtenir le nombre total de participants
     */
    public long getTotalParticipantsCount() {
        try {
            return repository.countAll();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Méthode pour vérifier s'il y a des participants à supprimer
     */
    public boolean hasParticipants() {
        return getTotalParticipantsCount() > 0;
    }

    public List<String> getAvailableStatuses() {
        return Arrays.asList("pending", "confirmed", "cancelled");
    }

    /**
     * Méthode pour vérifier si un participant existe avant suppression
     */
    public boolean participantExists(Long participantId) {
        if (participantId == null || participantId.longValue() <= 0) {
            return false;
        }

        try {
            List<EventParticipation> participations = repository.findAll();
            return participations.stream()
                    .anyMatch(p -> p != null && Objects.equals(p.getId(), participantId.longValue()));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Suppression sécurisée avec vérification d'existence
     */
    public boolean safeDeleteParticipant(Long participantId) {
        if (!participantExists(participantId)) {
            throw new IllegalArgumentException("Participant introuvable avec l'ID: " + participantId);
        }

        return deleteParticipant(participantId);
    }
}