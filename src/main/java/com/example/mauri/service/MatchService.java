package com.example.mauri.service;

import com.example.mauri.enums.MatchStatus;
import com.example.mauri.model.Match;
import com.example.mauri.model.dto.create.CreateMatchDTO;
import com.example.mauri.model.MatchResult;
import com.example.mauri.model.dto.response.MatchResponseDTO;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

public interface MatchService {

    List<MatchResponseDTO> getMatches();
    MatchResponseDTO getMatch(@NonNull String id);
    MatchResponseDTO createMatch(CreateMatchDTO createMatchDTO);
    void deleteMatch(@NonNull String id);
    Match addResult (String matchId, MatchResult matchResult);
    List<MatchResponseDTO> generateMatchesForLeague(String leagueId);
    Map<Integer, List<MatchResponseDTO>> getMatchesGroupedByRound(String leagueId);
    int getTotalMatchesCount(String leagueId);
    int getPlayedMatchesCount(String leagueId);
    void cancelResult(String matchId);
    List<MatchResponseDTO> getMatchesForPlayerInActiveSeason(String playerId, MatchStatus status);
    List<MatchResponseDTO> getMatchesForTeamInActiveSeason(String teamId, MatchStatus status);
}
