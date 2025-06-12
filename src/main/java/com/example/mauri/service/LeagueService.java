package com.example.mauri.service;

import com.example.mauri.model.League;
import com.example.mauri.model.dto.CreateLeagueDTO;
import lombok.NonNull;

import java.util.List;

public interface LeagueService {
    List<League> getAllLeagues();
    League getLeagueById(@NonNull String id);
    League createLeague(CreateLeagueDTO createLeagueDTO);
    void deleteLeagueById(@NonNull String id);
    League addParticipantsToLeague(String leagueId, List<String> participantIds);
    void removeParticipantFromLeague(String leagueId, String participantId);
    List<League> getLeaguesWithoutSeason();
    int progress(String leagueId);
}
