package com.example.mauri.service.impl;

import com.example.mauri.model.Match;
import com.example.mauri.model.MatchResult;
import com.example.mauri.repository.MatchRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchResultBackfillService {

    private final MatchRepository matchRepository;

    @Transactional
    public void fillMissingPoints() {

        List<Match> matches = matchRepository.findAll();

        for (Match match : matches) {

            MatchResult result = match.getResult();

            if (result == null) {
                continue;
            }

            // LEGACY logika = sets
            result.setPoints1(result.getScore1());
            result.setPoints2(result.getScore2());
        }
    }
}
