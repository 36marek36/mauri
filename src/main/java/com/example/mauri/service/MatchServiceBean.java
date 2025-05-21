package com.example.mauri.service;

import com.example.mauri.enums.LeagueStatus;
import com.example.mauri.enums.MatchStatus;
import com.example.mauri.enums.MatchType;
import com.example.mauri.model.*;
import com.example.mauri.model.dto.CreateMatchDTO;
import com.example.mauri.repository.LeagueRepository;
import com.example.mauri.repository.MatchRepository;
import com.example.mauri.repository.PlayerRepository;
import com.example.mauri.repository.TeamRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MatchServiceBean implements MatchService {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final LeagueRepository leagueRepository;

    public MatchServiceBean(MatchRepository matchRepository, TeamRepository teamRepository, PlayerRepository playerRepository, LeagueRepository leagueRepository) {
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.leagueRepository = leagueRepository;
    }


    @Override
    public List<Match> getMatches() {
        return matchRepository.findAll().stream().toList();
    }

    @Override
    public Match getMatch(@NonNull String id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No Match found with id: " + id));
    }

    @Override
    public Match createMatch(CreateMatchDTO createMatchDTO) {
        Match match = new Match();
        match.setId(UUID.randomUUID().toString());
        match.setMatchType(createMatchDTO.getMatchType());
        match.setLeagueId(createMatchDTO.getLeagueId());
        match.setStatus(MatchStatus.CREATED);

        switch (createMatchDTO.getMatchType()) {
            case SINGLES -> {
                match.setHomePlayer(playerRepository.findById(createMatchDTO.getPlayer1Id())
                        .orElseThrow(() -> new IllegalArgumentException("No Player found with id: " + createMatchDTO.getPlayer1Id())));
                match.setAwayPlayer(playerRepository.findById(createMatchDTO.getPlayer2Id())
                        .orElseThrow(() -> new IllegalArgumentException("No Player found with id: " + createMatchDTO.getPlayer2Id())));
            }
            case DOUBLES -> {
                match.setHomeTeam(teamRepository.findById(createMatchDTO.getTeam1Id())
                        .orElseThrow(() -> new IllegalArgumentException("No Team found with id: " + createMatchDTO.getTeam1Id())));
                match.setAwayTeam(teamRepository.findById(createMatchDTO.getTeam2Id())
                        .orElseThrow(() -> new IllegalArgumentException("No Team found with id: " + createMatchDTO.getTeam2Id())));
            }
            default -> throw new IllegalArgumentException("Unsupported MatchType: " + createMatchDTO.getMatchType());
        }
        return matchRepository.save(match);
    }

    @Override
    public void deleteMatch(@NonNull String id) {
        if (!matchRepository.existsById(id)) {
            throw new IllegalArgumentException("No Match found with id: " + id);
        }
        matchRepository.deleteById(id);
    }

    @Override
    public Match addResult(String matchId, MatchResult matchResult) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("No Match found with id: " + matchId));

        String winnerId = null;

        if (matchResult.getScratchedId() != null) {
            // Skrečovaný zápas
            winnerId = switch (match.getMatchType()) {
                case SINGLES -> matchResult.getScratchedId().equals(match.getHomePlayer().getId())
                        ? match.getAwayPlayer().getId()
                        : match.getHomePlayer().getId();
                case DOUBLES -> matchResult.getScratchedId().equals(match.getHomeTeam().getId())
                        ? match.getAwayTeam().getId()
                        : match.getHomeTeam().getId();
            };
        } else if (matchResult.getSetScores() != null && !matchResult.getSetScores().isEmpty()) {
            // Automatické očíslovanie setov
            for (int i = 0; i < matchResult.getSetScores().size(); i++) {
                matchResult.getSetScores().get(i).setSetNumber(i + 1); // 1-based index
            }
            // Výpočet skóre zo setov
            int setsWon1 = 0;
            int setsWon2 = 0;

            for (SetScore set : matchResult.getSetScores()) {
                if (set.getScore1() != null && set.getScore2() != null) {
                    if (set.getScore1() > set.getScore2()) {
                        setsWon1++;
                    } else if (set.getScore2() > set.getScore1()) {
                        setsWon2++;
                    }
                }
            }

            matchResult.setScore1(setsWon1);
            matchResult.setScore2(setsWon2);

            if (setsWon1 > setsWon2) {
                winnerId = switch (match.getMatchType()) {
                    case SINGLES -> match.getHomePlayer().getId();
                    case DOUBLES -> match.getHomeTeam().getId();
                };
            } else if (setsWon2 > setsWon1) {
                winnerId = switch (match.getMatchType()) {
                    case SINGLES -> match.getAwayPlayer().getId();
                    case DOUBLES -> match.getAwayTeam().getId();
                };
            }
        }

        matchResult.setWinnerId(winnerId);
        match.setResult(matchResult);
        return matchRepository.save(match);
    }

    @Override
    @Transactional
    public List<Match> generateMatchesForLeague(String leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("No League found with id: " + leagueId));

        if (matchRepository.existsByLeagueId(leagueId)) {
            throw new IllegalStateException("Matches already exists for League");
        }

        MatchType type = league.getLeagueType();
        List<Match> matches = new ArrayList<>();

        if (type == MatchType.SINGLES) {
            List<Player> players = league.getPlayers();
            for (int i = 0; i < players.size(); i++) {
                for (int j = i + 1; j < players.size(); j++) {
                    Player p1 = players.get(i);
                    Player p2 = players.get(j);

                    boolean isEven = (i + j) % 2 == 0;

                    Match match = new Match();
                    match.setId(UUID.randomUUID().toString());
                    match.setLeagueId(leagueId);
                    match.setMatchType(type);
                    match.setStatus(MatchStatus.CREATED);
                    match.setHomePlayer(isEven ? p1 : p2);
                    match.setAwayPlayer(isEven ? p2 : p1);
                    matches.add(match);
                }

            }
        } else if (type == MatchType.DOUBLES) {
            List<Team> teams = league.getTeams();
            for (int i = 0; i < teams.size(); i++) {
                for (int j = i + 1; j < teams.size(); j++) {
                    Team t1 = teams.get(i);
                    Team t2 = teams.get(j);
                    boolean isEven = (i + j) % 2 == 0;
                    Match match = new Match();
                    match.setId(UUID.randomUUID().toString());
                    match.setLeagueId(leagueId);
                    match.setMatchType(type);
                    match.setStatus(MatchStatus.CREATED);
                    match.setHomeTeam(isEven ? t1 : t2);
                    match.setAwayTeam(isEven ? t2 : t1);
                    matches.add(match);
                }
            }
        }

        league.setStatus(LeagueStatus.ACTIVE);

        matchRepository.saveAll(matches);

        return matches;
    }

    @Override
    public List<Match> getMatchesForLeague(String leagueId) {
        return matchRepository.findByLeagueId(leagueId);
    }
}
