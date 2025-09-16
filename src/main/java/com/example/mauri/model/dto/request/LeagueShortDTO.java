package com.example.mauri.model.dto.request;

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
}
