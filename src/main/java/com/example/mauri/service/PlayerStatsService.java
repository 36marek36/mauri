package com.example.mauri.service;

import com.example.mauri.model.Match;
import com.example.mauri.model.Player;
import com.example.mauri.model.dto.response.PlayerStatsDTO;

import java.util.List;
import java.util.Map;

public interface PlayerStatsService {
    PlayerStatsDTO getPlayerStats(String leagueId, String playerId);
    List<PlayerStatsDTO> getAllStatsForLeague(String leagueId);
    Map<String, Integer> calculatePlayerProgress(List<Match> matches, List<Player> players);
}
