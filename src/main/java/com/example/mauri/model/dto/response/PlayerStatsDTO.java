package com.example.mauri.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerStatsDTO {

    private String playerId;
    private String playerName;
    private int matches;
    private int wins;
    private int losses;
    private int setsWon;
    private int setsLost;
    private int leagueProgress;
    private boolean droppedFromLeague;
}
