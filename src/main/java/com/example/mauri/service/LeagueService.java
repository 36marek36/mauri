package com.example.mauri.service;

import com.example.mauri.enums.MatchType;
import com.example.mauri.model.League;
import com.example.mauri.model.dto.create.CreateLeagueDTO;
import com.example.mauri.model.dto.response.LeagueResponseDTO;
import lombok.NonNull;

import java.util.List;

public interface LeagueService {
    List<LeagueResponseDTO> getAllLeagues();
    LeagueResponseDTO getLeagueById(String id);
    LeagueResponseDTO createLeague(CreateLeagueDTO createLeagueDTO);
    void deleteLeagueById(@NonNull String id);
    String addParticipantsToLeague(String leagueId, List<String> participantIds);
    void removeParticipantFromLeague(String leagueId, String participantId);
    int progress(String leagueId);
    List<LeagueResponseDTO> getLeaguesForPlayer(String playerId);
    String getLeagueWinnerName(String leagueId, MatchType leagueType);
    void finishLeague(String leagueId);
    LeagueResponseDTO mapLeagueToDTO(League league);
}
