package com.example.mauri.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class VolleyMatchResult {
    private Integer homeTeamScore;
    private Integer awayTeamScore;
    private String winnerId;
    private Integer homeTeamPoints;
    private Integer awayTeamPoints;

    @ElementCollection
    @CollectionTable(name = "volleyMatch_set_scores", joinColumns = @JoinColumn(name = "volleyMatchId"))
    private List<SetScore> setScores=new ArrayList<>();
}
