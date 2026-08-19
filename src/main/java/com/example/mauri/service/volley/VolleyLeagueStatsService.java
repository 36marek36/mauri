package com.example.mauri.service.volley;

import com.example.mauri.model.dto.response.VolleyTeamStatsDTO;

import java.util.List;

public interface VolleyLeagueStatsService {
    VolleyTeamStatsDTO getTeamStats(String leagueId, String teamId);
    List<VolleyTeamStatsDTO> getAllStatsForLeague(String leagueId);
}
