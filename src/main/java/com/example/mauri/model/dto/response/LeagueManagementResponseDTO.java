package com.example.mauri.model.dto.response;

import com.example.mauri.enums.LeagueStatus;
import com.example.mauri.enums.MatchType;
import com.example.mauri.model.dto.request.PlayerShortDTO;
import com.example.mauri.model.dto.request.TeamShortDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LeagueManagementResponseDTO {
    private String id;
    private String name;
    private MatchType leagueType;
    private LeagueStatus leagueStatus;
    private List<PlayerShortDTO> players;
    private List<TeamShortDTO> teams;
}
