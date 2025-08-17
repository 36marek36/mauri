package com.example.mauri.service.impl;

import com.example.mauri.model.Match;
import com.example.mauri.model.MatchResult;
import com.example.mauri.model.SetScore;
import com.example.mauri.service.MatchResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchResultServiceBean implements MatchResultService {
    @Override
    public MatchResult processResult(Match match, MatchResult inputResult) {
        if (inputResult.getScratchedId() != null) {
            inputResult.setSetScores(generateScratchResult(match, inputResult.getScratchedId()));
        } else {
            validateSetScores(inputResult.getSetScores());
        }

        if (!inputResult.getSetScores().isEmpty()) {
            numberSets(inputResult.getSetScores());
            calculateScore(inputResult);
            determineWinner(match, inputResult);
        }

        return inputResult;
    }

    private List<SetScore> generateScratchResult(Match match, String scratchedId) {
        boolean scratchedIsHome = switch (match.getMatchType()) {
            case SINGLES -> scratchedId.equals(match.getHomePlayer().getId());
            case DOUBLES -> scratchedId.equals(match.getHomeTeam().getId());
        };

        List<SetScore> sets = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            SetScore set = new SetScore();
            set.setSetNumber(i);
            if (scratchedIsHome) {
                set.setScore1(0);
                set.setScore2(6);
            } else {
                set.setScore1(6);
                set.setScore2(0);
            }
            sets.add(set);
        }
        return sets;
    }

    private void validateSetScores(List<SetScore> setScores) {
        if (setScores == null || setScores.isEmpty()) {
            throw new IllegalArgumentException("Musíte zadať aspoň jeden set.");
        }

        for (SetScore set : setScores) {
            Integer s1 = set.getScore1();
            Integer s2 = set.getScore2();

            if (s1 == null || s2 == null) {
                throw new IllegalArgumentException("Set nemá vyplnené skóre.");
            }

            if (s1 < 0 || s2 < 0 || s1 > 30 || s2 > 30) {
                throw new IllegalArgumentException("Skóre musí byť v rozsahu 0–30.");
            }

            if (s1.equals(s2)) {
                throw new IllegalArgumentException("Set nemôže skončiť remízou.");
            }

            int diff = Math.abs(s1 - s2);
            boolean tiebreak = (s1 == 7 && s2 == 6) || (s1 == 6 && s2 == 7);
            if (diff < 2 && !tiebreak) {
                throw new IllegalArgumentException("Rozdiel v skóre musí byť aspoň 2 body, okrem tiebreaku.");
            }
        }
    }

    private void numberSets(List<SetScore> setScores) {
        for (int i = 0; i < setScores.size(); i++) {
            setScores.get(i).setSetNumber(i + 1);
        }
    }

    private void calculateScore(MatchResult matchResult) {
        int setsWon1 = 0;
        int setsWon2 = 0;

        for (SetScore set : matchResult.getSetScores()) {
            if (set.getScore1() > set.getScore2()) {
                setsWon1++;
            } else {
                setsWon2++;
            }
        }

        matchResult.setScore1(setsWon1);
        matchResult.setScore2(setsWon2);
    }

    private void determineWinner(Match match, MatchResult matchResult) {
        if (matchResult.getScore1() > matchResult.getScore2()) {
            String winnerId = switch (match.getMatchType()) {
                case SINGLES -> match.getHomePlayer().getId();
                case DOUBLES -> match.getHomeTeam().getId();
            };
            matchResult.setWinnerId(winnerId);
        } else if (matchResult.getScore2() > matchResult.getScore1()) {
            String winnerId = switch (match.getMatchType()) {
                case SINGLES -> match.getAwayPlayer().getId();
                case DOUBLES -> match.getAwayTeam().getId();
            };
            matchResult.setWinnerId(winnerId);
        }
    }
}
