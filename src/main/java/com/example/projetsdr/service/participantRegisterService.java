package com.example.projetsdr.service;

import com.example.projetsdr.model.Participant;
import com.example.projetsdr.repository.ParticipantRegisterRepository;

public class participantRegisterService {
    private final ParticipantRegisterRepository participantRepo;

    public participantRegisterService(ParticipantRegisterRepository participantRepo) {
        this.participantRepo = participantRepo;
    }

    public boolean createParticipant(Participant participant) {
        return participantRepo.save(participant);
    }
}