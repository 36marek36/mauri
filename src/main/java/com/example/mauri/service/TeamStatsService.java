package com.example.mauri.service;

import com.example.mauri.model.dto.response.TeamStatsDTO;

import java.util.List;

public interface TeamStatsService {
    TeamStatsDTO getTeamStats(String leagueId, String teamId);
    List<TeamStatsDTO> getAllStatsForLeague(String leagueId);
    int teamProgress(String leagueId, String teamId);
}
