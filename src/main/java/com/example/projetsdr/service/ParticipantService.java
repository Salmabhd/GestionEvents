package com.example.projetsdr.service;

import com.example.projetsdr.model.Participant;
import com.example.projetsdr.repository.ParticipantRepository;

public class ParticipantService {
    private final ParticipantRepository participantRepo;

    public ParticipantService(ParticipantRepository participantRepo) {
        this.participantRepo = participantRepo;
    }

    public Participant login(String email, String password) {
        return participantRepo.findByEmailAndPassword(email, password);
    }


}