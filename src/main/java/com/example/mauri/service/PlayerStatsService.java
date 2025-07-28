package com.example.mauri.service;

import com.example.mauri.model.dto.PlayerStatsDTO;

import java.util.List;

public interface PlayerStatsService {
    PlayerStatsDTO getPlayerStats(String leagueId, String playerId);
    List<PlayerStatsDTO> getAllStatsForLeague(String leagueId);
}
