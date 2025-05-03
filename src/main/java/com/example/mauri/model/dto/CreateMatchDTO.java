package com.example.mauri.model.dto;

import com.example.mauri.enums.MatchType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMatchDTO {
    private MatchType matchType;

    private String player1Id;
    private String player2Id;

    private String team1Id;
    private String team2Id;

    private String leagueId;
}
