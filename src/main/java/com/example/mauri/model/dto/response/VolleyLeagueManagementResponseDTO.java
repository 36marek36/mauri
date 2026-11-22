package com.example.mauri.model.dto.response;

import com.example.mauri.enums.LeagueStatus;
import com.example.mauri.model.dto.request.VolleyTeamShortDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class VolleyLeagueManagementResponseDTO {
    private String id;
    private String name;
    private LeagueStatus leagueStatus;
    private List<VolleyTeamShortDTO> teams;
}
