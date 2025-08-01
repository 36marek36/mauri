package com.example.mauri.service;

import com.example.mauri.enums.MatchStatus;
import com.example.mauri.model.Match;
import com.example.mauri.model.dto.CreateMatchDTO;
import com.example.mauri.model.MatchResult;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

public interface MatchService {

    List<Match> getMatches();
    Match getMatch(@NonNull String id);
    Match createMatch(CreateMatchDTO createMatchDTO);
    void deleteMatch(@NonNull String id);
    Match addResult (String matchId, MatchResult matchResult);
    List<Match> generateMatchesForLeague(String leagueId);
    List<Match> getMatchesForLeague(String leagueId);
    Map<Integer, List<Match>> getMatchesGroupedByRound(String leagueId);
    List<Match> getPlayedMatchesForLeague(String leagueId);
    void cancelResult(String matchId);
    List<Match> getMatchesForPlayerInActiveSeason(String playerId, MatchStatus status);
    List<Match> getMatchesForTeamInActiveSeason(String teamId, MatchStatus status);
}
