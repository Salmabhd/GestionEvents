package com.example.projetsdr.service;

import com.example.projetsdr.model.EventParticipation;
import com.example.projetsdr.repository.EventListeParticipantRepository;
import java.util.Arrays;
import java.util.List;

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

    public boolean deleteParticipation(long id) {
        try {
            return repository.deleteById(id);
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> getAvailableStatuses() {
        return Arrays.asList("pending", "confirmed", "cancelled");
    }
}