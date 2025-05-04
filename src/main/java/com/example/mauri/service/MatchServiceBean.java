package com.example.mauri.service;

import com.example.mauri.model.Match;
import com.example.mauri.model.SetScore;
import com.example.mauri.model.dto.CreateMatchDTO;
import com.example.mauri.model.MatchResult;
import com.example.mauri.repository.MatchRepository;
import com.example.mauri.repository.PlayerRepository;
import com.example.mauri.repository.TeamRepository;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MatchServiceBean implements MatchService {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    public MatchServiceBean(MatchRepository matchRepository, TeamRepository teamRepository, PlayerRepository playerRepository) {
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
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

        switch (createMatchDTO.getMatchType()) {
            case SINGLES -> {
                match.setPlayer1(playerRepository.findById(createMatchDTO.getPlayer1Id())
                        .orElseThrow(() -> new IllegalArgumentException("No Player found with id: " + createMatchDTO.getPlayer1Id())));
                match.setPlayer2(playerRepository.findById(createMatchDTO.getPlayer2Id())
                        .orElseThrow(() -> new IllegalArgumentException("No Player found with id: " + createMatchDTO.getPlayer2Id())));
            }
            case DOUBLES -> {
                match.setTeam1(teamRepository.findById(createMatchDTO.getTeam1Id())
                        .orElseThrow(() -> new IllegalArgumentException("No Team found with id: " + createMatchDTO.getTeam1Id())));
                match.setTeam2(teamRepository.findById(createMatchDTO.getTeam2Id())
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
                case SINGLES -> matchResult.getScratchedId().equals(match.getPlayer1().getId())
                        ? match.getPlayer2().getId()
                        : match.getPlayer1().getId();
                case DOUBLES -> matchResult.getScratchedId().equals(match.getTeam1().getId())
                        ? match.getTeam2().getId()
                        : match.getTeam1().getId();
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
                    case SINGLES -> match.getPlayer1().getId();
                    case DOUBLES -> match.getTeam1().getId();
                };
            } else if (setsWon2 > setsWon1) {
                winnerId = switch (match.getMatchType()) {
                    case SINGLES -> match.getPlayer2().getId();
                    case DOUBLES -> match.getTeam2().getId();
                };
            }
        }

        matchResult.setWinnerId(winnerId);
        match.setResult(matchResult);
        return matchRepository.save(match);
    }
}
