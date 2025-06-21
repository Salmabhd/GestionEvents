package com.example.projetsdr.service;

import com.example.projetsdr.model.EventParticipation;
import com.example.projetsdr.repository.EventParticipationRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class EventParticipationService {

    @Inject
    private EventParticipationRepository repository;

    public boolean register(EventParticipation participation) {
        if (repository.alreadyExists(participation.getEvent().getId(), participation.getParticipantEmail())) {
            return false;
        }
        repository.save(participation);
        return true;
    }
}
