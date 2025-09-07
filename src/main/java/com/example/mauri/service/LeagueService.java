package com.example.mauri.service;

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
    String removeParticipantFromLeague(String leagueId, String participantId);
    String dropParticipantFromLeague(String leagueId, String participantId);
    List<LeagueResponseDTO> getLeaguesForPlayer(String playerId);
    void finishLeague(String leagueId);
    LeagueResponseDTO mapLeagueToDTO(League league);
}
