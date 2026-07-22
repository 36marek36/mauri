package com.example.mauri.model.dto.response;

import com.example.mauri.enums.LeagueStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolleyLeagueResponseDTO {
    private String leagueId;
    private String leagueName;
    private Integer seasonYear;
    private LeagueStatus leagueStatus;
    private List<VolleyTeamResponseDTO> teams;
}
