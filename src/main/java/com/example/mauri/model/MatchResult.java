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

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchResult {
    private Integer score1;
    private Integer score2;
    private String scratchedId;
    private String winnerId;

    @ElementCollection
    @CollectionTable(name = "match_set_scores", joinColumns = @JoinColumn(name = "match_id"))
    private List<SetScore> setScores=new ArrayList<>();
}
