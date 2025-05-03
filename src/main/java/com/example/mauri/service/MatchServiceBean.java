package com.example.mauri.service;

import com.example.mauri.enums.MatchType;
import com.example.mauri.model.Match;
import com.example.mauri.model.Player;
import com.example.mauri.model.Team;
import com.example.mauri.model.dto.CreateMatchDTO;
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

        if (createMatchDTO.getMatchType() == MatchType.SINGLES) {
            Player player1 = playerRepository.findById(createMatchDTO.getPlayer1Id())
                    .orElseThrow(() -> new RuntimeException("Player1 not found"));
            Player player2 = playerRepository.findById(createMatchDTO.getPlayer2Id())
                    .orElseThrow(() -> new RuntimeException("Player2 not found"));
            match.setPlayer1(player1);
            match.setPlayer2(player2);
        } else if (createMatchDTO.getMatchType() == MatchType.DOUBLES) {
            Team team1 = teamRepository.findById(createMatchDTO.getTeam1Id())
                    .orElseThrow(() -> new RuntimeException("Team1 not found"));
            Team team2 = teamRepository.findById(createMatchDTO.getTeam2Id())
                    .orElseThrow(() -> new RuntimeException("Team2 not found"));
            match.setTeam1(team1);
            match.setTeam2(team2);

        } else {
            throw new IllegalArgumentException("Unsupported match type: " + createMatchDTO.getMatchType());
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
    public Match addResult(String matchId, String scratchedId, Integer score1, Integer score2) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("No Match found with id: " + matchId));

        match.setScratchedId(scratchedId);
        match.setScore1(score1);
        match.setScore2(score2);

        if (scratchedId != null) {
            String winnerId = switch (match.getMatchType()) {
                case SINGLES -> scratchedId.equals(match.getPlayer1().getId())
                        ? match.getPlayer2().getId()
                        : match.getPlayer1().getId();
                case DOUBLES -> scratchedId.equals(match.getTeam1().getId())
                        ? match.getTeam2().getId()
                        : match.getTeam1().getId();
            };
            match.setWinnerId(winnerId);
        }

        return matchRepository.save(match);
    }
}
