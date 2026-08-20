package com.example.mauri.model;

import com.example.mauri.enums.Sport;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class MatchActivity {

    @Id
    private String matchId;

    @Enumerated(EnumType.STRING)
    private Sport sport;

    private Instant createdAt;

}
