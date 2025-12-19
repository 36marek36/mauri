package com.example.mauri.model.dto.request;

import com.example.mauri.enums.LeagueStatus;
import com.example.mauri.enums.MatchType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class LeagueShortDTO {
    private String leagueId;
    private String leagueName;
    private Integer seasonYear;
    private MatchType leagueType;
    private LeagueStatus leagueStatus;
}
