package com.example.mauri.service.volley.impl;

import com.example.mauri.exception.InvalidMatchResultException;
import com.example.mauri.model.*;
import com.example.mauri.service.volley.VolleyMatchResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VolleyMatchResultServiceBean implements VolleyMatchResultService {

    @Override
    public VolleyMatchResult processResult(VolleyMatch volleyMatch, VolleyMatchResult result) {

        validateSetScores(result.getSetScores());


        if (!result.getSetScores().isEmpty()) {
            numberSets(result.getSetScores());
            calculateScore(result);
            calculatePoints(result);
            validateMatch(result);
            determineWinner(volleyMatch, result);
        }

        return result;
    }

    private void validateSetScores(List<SetScore> setScores) {
        if (setScores == null || setScores.isEmpty()) {
            throw new InvalidMatchResultException("Musíte zadať aspoň jeden set.");
        }

        if (setScores.size() < 3 || setScores.size() > 5) {
            throw new InvalidMatchResultException("Zápas môže mať 3 až 5 setov.");
        }
        int setsWon1 = 0;
        int setsWon2 = 0;

        for (SetScore setScore : setScores) {

            // Ak už pred aktuálnym setom niekto vyhral zápas,
            // ďalší set je neplatný.
            if (setsWon1 == 3 || setsWon2 == 3) {
                throw new InvalidMatchResultException(
                        "Po rozhodujúcom sete už nemožno zadávať ďalšie sety.");
            }

            int s1 = setScore.getScore1();
            int s2 = setScore.getScore2();

            if (s1 < 0 || s2 < 0) {
                throw new InvalidMatchResultException("Skóre nemôže byť záporné.");
            }

            if (s1 == s2) {
                throw new InvalidMatchResultException("Set nemôže skončiť remízou.");
            }

            int max = Math.max(s1, s2);
            int min = Math.min(s1, s2);

            if (max < 21) {
                throw new InvalidMatchResultException("Víťaz setu musí mať aspoň 21 bodov.");
            }

            if (max - min < 2) {
                throw new InvalidMatchResultException("Víťaz musí vyhrať minimálne o 2 body.");
            }

            // Započítanie víťaza setu
            if (s1 > s2) {
                setsWon1++;
            } else {
                setsWon2++;
            }
        }
    }

    private void numberSets(List<SetScore> setScores) {
        for (int i = 0; i < setScores.size(); i++) {
            setScores.get(i).setSetNumber(i + 1);
        }
    }

    private void calculateScore(VolleyMatchResult matchResult) {
        int setsWon1 = 0;
        int setsWon2 = 0;

        for (SetScore set : matchResult.getSetScores()) {
            if (set.getScore1() > set.getScore2()) {
                setsWon1++;
            } else {
                setsWon2++;
            }
        }

        matchResult.setHomeTeamScore(setsWon1);
        matchResult.setAwayTeamScore(setsWon2);
    }

    private void validateMatch(VolleyMatchResult result) {

        int s1 = result.getHomeTeamScore();
        int s2 = result.getAwayTeamScore();

        boolean valid =
                (s1 == 3 && s2 >= 0 && s2 <= 2) ||
                        (s2 == 3 && s1 >= 0 && s1 <= 2);

        if (!valid) {
            throw new InvalidMatchResultException("Neplatný výsledok zápasu.");
        }
    }

    private void determineWinner(VolleyMatch match, VolleyMatchResult matchResult) {
        int score1 = matchResult.getHomeTeamScore();
        int score2 = matchResult.getAwayTeamScore();

        String winnerId;
        if (score1 > score2) {
            winnerId = match.getHomeTeam().getId();
        } else {
            winnerId = match.getAwayTeam().getId();
        }
        matchResult.setWinnerId(winnerId);
    }

    private void calculatePoints(VolleyMatchResult matchResult) {

        int setsWon1 = matchResult.getHomeTeamScore();
        int setsWon2 = matchResult.getAwayTeamScore();

        int points1;
        int points2;

        if (setsWon1 > setsWon2) {

            if (setsWon2 <= 1) {
                points1 = 3;
                points2 = 0;
            } else {
                points1 = 2;
                points2 = 1;
            }

        } else {

            if (setsWon1 <= 1) {
                points2 = 3;
                points1 = 0;
            } else {
                points2 = 2;
                points1 = 1;
            }
        }

        matchResult.setHomeTeamPoints(points1);
        matchResult.setAwayTeamPoints(points2);

    }
}
