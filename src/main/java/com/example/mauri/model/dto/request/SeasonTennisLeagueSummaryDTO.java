package com.example.mauri.model.dto.request;

import com.example.mauri.enums.MatchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonTennisLeagueSummaryDTO {
    private String id;
    private String name;
    private long participants;
    private MatchType leagueType;
    private String winnerName;
    private int leagueProgress;
}
