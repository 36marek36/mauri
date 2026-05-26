package com.example.mauri.service;

import com.example.mauri.model.Match;
import com.example.mauri.model.Team;
import com.example.mauri.model.dto.response.TeamStatsDTO;

import java.util.List;
import java.util.Map;

public interface TeamStatsService {
    TeamStatsDTO getTeamStats(String leagueId, String teamId);
    List<TeamStatsDTO> getAllStatsForLeague(String leagueId);
    Map<String, Integer> calculateTeamProgress(List<Match> matches, List<Team> teams);
}
