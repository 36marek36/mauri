package com.example.mauri.model.dto;

import com.example.mauri.enums.LeagueStatus;
import com.example.mauri.enums.MatchType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeagueDTO {
    private String leagueId;
    private String leagueName;
    private Integer seasonYear;
    private MatchType leagueType;
    private LeagueStatus leagueStatus;
    private int totalPlayers;
    private int totalTeams;
}
