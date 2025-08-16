package com.example.mauri.service.impl;

import com.example.mauri.enums.MatchStatus;
import com.example.mauri.model.*;
import com.example.mauri.model.dto.PlayerStatsDTO;
import com.example.mauri.repository.LeagueRepository;
import com.example.mauri.repository.MatchRepository;
import com.example.mauri.service.PlayerService;
import com.example.mauri.service.PlayerStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerStatsServiceBean implements PlayerStatsService {

    private final MatchRepository matchRepository;
    private final LeagueRepository leagueRepository;
    private final PlayerService playerService;

    public PlayerStatsDTO getPlayerStats(String leagueId, String playerId) {
        List<Match> matches = matchRepository.findByLeagueIdAndPlayer(leagueId, playerId);

        Player player = playerService.getPlayer(playerId);
        String playerName = player.getFirstName() + " " + player.getLastName();

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

            boolean isHome = match.getHomePlayer().getId().equals(playerId);
            int playerSets = isHome ? matchResult.getScore1() : matchResult.getScore2();
            int opponentSets = isHome ? matchResult.getScore2() : matchResult.getScore1();

            matchesPlayed++;
            setsWon += playerSets;
            setsLost += opponentSets;

            if (matchResult.getWinnerId().equals(playerId)) {
                wins++;
            } else {
                losses++;
            }
        }
        return PlayerStatsDTO.builder()
                .playerId(playerId)
                .playerName(playerName)
                .matches(matchesPlayed)
                .wins(wins)
                .losses(losses)
                .setsWon(setsWon)
                .setsLost(setsLost)
                .build();
    }

    public List<PlayerStatsDTO> getAllStatsForLeague(String leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));


        List<Player> players = league.getPlayers();
        List<PlayerStatsDTO> statsList = new ArrayList<>();

        for (Player player : players) {
            PlayerStatsDTO stats = getPlayerStats(leagueId, player.getId());
            statsList.add(stats);
        }

        statsList.sort(Comparator.comparingInt(PlayerStatsDTO::getSetsWon).reversed());
        return statsList;
    }
}

