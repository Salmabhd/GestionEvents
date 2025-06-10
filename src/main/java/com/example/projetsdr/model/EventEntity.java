package com.example.projetsdr.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    private String venue;
    private String city;
    private String category;
    private String status;

    private Integer maxParticipants;
    private Integer ticketsSold;
    private Double ticketPrice;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters & Setters
}
