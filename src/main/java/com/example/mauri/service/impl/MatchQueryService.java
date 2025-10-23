package com.example.mauri.service.impl;

import com.example.mauri.enums.MatchStatus;
import com.example.mauri.model.Match;
import com.example.mauri.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchQueryService {

    private final MatchRepository matchRepository;

    public List<Match> getEvaluatedMatches(String leagueId) {
        List<Match> finished = matchRepository.findByLeagueIdAndStatus(leagueId, MatchStatus.FINISHED);
        List<Match> scratched = matchRepository.findByLeagueIdAndStatus(leagueId, MatchStatus.SCRATCHED);

        List<Match> all = new ArrayList<>();
        all.addAll(finished);
        all.addAll(scratched);

        return all;
    }
}
