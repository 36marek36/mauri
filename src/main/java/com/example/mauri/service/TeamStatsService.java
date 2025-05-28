package com.example.mauri.service;

import com.example.mauri.enums.MatchStatus;
import com.example.mauri.model.Match;
import com.example.mauri.model.MatchResult;
import com.example.mauri.model.dto.TeamStatsDTO;
import com.example.mauri.repository.MatchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamStatsService {

    private final MatchRepository matchRepository;

    public TeamStatsService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public TeamStatsDTO getTeamStats(String leagueId, String teamId) {
        List<Match> matches = matchRepository.findByLeagueIdAndTeam(leagueId, teamId);

        int matchesPlayed = 0;
        int wins = 0;
        int losses = 0;
        int setsWon = 0;
        int setsLost = 0;

        for (Match match : matches) {
            if (match.getStatus() != MatchStatus.FINISHED || match.getResult() == null) {
                continue;
            }

            MatchResult matchResult = match.getResult();

            boolean isHome = match.getHomeTeam().getId().equals(teamId);
            int teamSets = isHome ? matchResult.getScore1() : matchResult.getScore2();
            int opponentSets = isHome ? matchResult.getScore2() : matchResult.getScore1();

            matchesPlayed++;
            setsWon += teamSets;
            setsLost += opponentSets;

            if (matchResult.getWinnerId().equals(teamId)) {
                wins++;
            } else {
                losses++;
            }
        }
        return new TeamStatsDTO(teamId, matchesPlayed, wins, losses, setsWon, setsLost);
    }
}
