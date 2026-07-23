package com.example.mauri.service.volley;

import com.example.mauri.model.dto.create.CreateVolleyLeagueDTO;
import com.example.mauri.model.dto.response.VolleyLeagueResponseDTO;

import java.util.List;

public interface VolleyLeagueService {
    List<VolleyLeagueResponseDTO> getLeagues();
    VolleyLeagueResponseDTO createLeague(CreateVolleyLeagueDTO createVolleyLeagueDTO);
    void deleteLeague(String id);
    String addTeamsToLeague(String leagueId, List<String> teamIds);
    String removeTeamFromLeague(String leagueId, String teamId);
    String startLeague(String leagueId);
    String finishLeague(String leagueId);
}
