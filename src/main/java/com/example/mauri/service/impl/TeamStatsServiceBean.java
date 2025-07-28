package com.example.mauri.service.impl;

import com.example.mauri.enums.MatchStatus;
import com.example.mauri.model.*;
import com.example.mauri.model.dto.TeamStatsDTO;
import com.example.mauri.repository.LeagueRepository;
import com.example.mauri.repository.MatchRepository;
import com.example.mauri.service.TeamService;
import com.example.mauri.service.TeamStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamStatsServiceBean implements TeamStatsService {

    private final MatchRepository matchRepository;
    private final LeagueRepository leagueRepository;
    private final TeamService teamService;

    public TeamStatsDTO getTeamStats(String leagueId, String teamId) {

        List<Match> matches = matchRepository.findByLeagueIdAndTeam(leagueId, teamId);

        Team team = teamService.getTeamById(teamId);
        String teamName = team.getPlayer1().getLastName()+" / "+team.getPlayer2().getLastName();


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
        return new TeamStatsDTO(teamId, teamName,matchesPlayed, wins, losses, setsWon, setsLost);
    }

    public List<TeamStatsDTO> getAllStatsForLeague(String leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));


        List<Team> teams = league.getTeams();
        List<TeamStatsDTO> statsList = new ArrayList<>();

        for (Team team : teams) {
            TeamStatsDTO stats = getTeamStats(leagueId, team.getId());
            statsList.add(stats);
        }

        statsList.sort(Comparator.comparingInt(TeamStatsDTO::getSetsWon).reversed());
        return statsList;
    }
}
