package com.example.mauri.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStatsDTO {

    private String playerId;
    private int matches;
    private int wins;
    private int losses;
    private int setsWon;
    private int setsLost;
}
