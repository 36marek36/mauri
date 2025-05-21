package com.example.mauri.model.dto;

import com.example.mauri.enums.MatchType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeagueDTO {
    private String name;
    private MatchType leagueType;
    private String seasonId;
}
