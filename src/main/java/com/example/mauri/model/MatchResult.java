package com.example.mauri.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchResult {
    private Integer score1;
    private Integer score2;
    private String scratchedId;
    private String winnerId;
}
