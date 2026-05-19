package com.example.mauri.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class MatchActivity {

    @Id
    private String matchId;

    private Instant createdAt;
}
