package com.example.mauri.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamStatsDTO {
    private String teamId;
    private String teamName;
    private int matches;
    private int wins;
    private int losses;
    private int setsWon;
    private int setsLost;
    private int leagueProgress;
}
