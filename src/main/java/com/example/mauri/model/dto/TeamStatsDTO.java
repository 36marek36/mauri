package com.example.mauri.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamStatsDTO {
    private String teamId;
    private String teamName;
    private int matches;
    private int wins;
    private int losses;
    private int setsWon;
    private int setsLost;
}
