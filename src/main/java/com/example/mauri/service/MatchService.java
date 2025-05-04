package com.example.mauri.service;

import com.example.mauri.model.Match;
import com.example.mauri.model.dto.CreateMatchDTO;
import com.example.mauri.model.MatchResult;
import lombok.NonNull;

import java.util.List;

public interface MatchService {

    List<Match> getMatches();
    Match getMatch(@NonNull String id);
    Match createMatch(CreateMatchDTO createMatchDTO);
    void deleteMatch(@NonNull String id);
    Match addResult (String matchId, MatchResult matchResult);
}
