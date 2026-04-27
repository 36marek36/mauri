package com.example.mauri.service.impl;

import com.example.mauri.exception.InvalidMatchResultException;
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
            validateMatch(inputResult);
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
            throw new InvalidMatchResultException("Musíte zadať aspoň jeden set.");
        }

        if (setScores.size() < 2) {
            throw new InvalidMatchResultException("Zápas musí obsahovať aspoň 2 sety.");
        }

        for (SetScore set : setScores) {

            int s1 = set.getScore1();
            int s2 = set.getScore2();

            if (s1 < 0 || s2 < 0) {
                throw new InvalidMatchResultException("Skóre nemôže byť záporné.");
            }

            if (s1 == s2) {
                throw new InvalidMatchResultException("Set nemôže skončiť remízou.");
            }

            int max = Math.max(s1, s2);
            int min = Math.min(s1, s2);

            boolean normalSet = (max == 6 && min <= 4);
            boolean extendedSet = (max == 7 && (min == 5 || min == 6));

            if (!normalSet && !extendedSet) {
                throw new InvalidMatchResultException("Neplatný výsledok setu.");
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

    private void validateMatch(MatchResult result) {

        int s1 = result.getScore1();
        int s2 = result.getScore2();

        if (s1 == s2) {
            throw new InvalidMatchResultException("Zápas nemôže skončiť remízou – musíte zadať rozhodujúci set.");
        }
    }

    private void determineWinner(Match match, MatchResult matchResult) {
        int score1 = matchResult.getScore1();
        int score2 = matchResult.getScore2();

        String winnerId;
        if (score1 > score2) {
            winnerId = switch (match.getMatchType()) {
                case SINGLES -> match.getHomePlayer().getId();
                case DOUBLES -> match.getHomeTeam().getId();
            };
        } else {
            winnerId = switch (match.getMatchType()) {
                case SINGLES -> match.getAwayPlayer().getId();
                case DOUBLES -> match.getAwayTeam().getId();
            };
        }
        matchResult.setWinnerId(winnerId);
    }
}
