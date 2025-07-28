package com.example.mauri.service.impl;

import com.example.mauri.enums.LeagueStatus;
import com.example.mauri.enums.MatchStatus;
import com.example.mauri.enums.MatchType;
import com.example.mauri.model.*;
import com.example.mauri.model.dto.CreateMatchDTO;
import com.example.mauri.repository.LeagueRepository;
import com.example.mauri.repository.MatchRepository;
import com.example.mauri.repository.PlayerRepository;
import com.example.mauri.repository.TeamRepository;
import com.example.mauri.service.MatchService;
import com.example.mauri.service.RoundRobinPlayersService;
import com.example.mauri.service.RoundRobinTeamsService;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchServiceBean implements MatchService {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final LeagueRepository leagueRepository;
    private final RoundRobinPlayersService roundRobinPlayersService;
    private final RoundRobinTeamsService roundRobinTeamsService;

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

        // Ak je skreč, nastavíme sety 6:0, 6:0 pre víťaza
        if (matchResult.getScratchedId() != null) {
            List<SetScore> sets = new ArrayList<>();
            boolean scratchedIsHome = false;

            switch (match.getMatchType()) {
                case SINGLES -> scratchedIsHome = matchResult.getScratchedId().equals(match.getHomePlayer().getId());
                case DOUBLES -> scratchedIsHome = matchResult.getScratchedId().equals(match.getHomeTeam().getId());
            }

            for (int i = 1; i <= 2; i++) {
                SetScore set = new SetScore();
                set.setSetNumber(i);
                if (scratchedIsHome) {
                    // Domáci hráč/tím skrečuje, víťaz je hosť
                    set.setScore1(0);
                    set.setScore2(6);
                } else {
                    // Hosť skrečuje, víťaz je domáci
                    set.setScore1(6);
                    set.setScore2(0);
                }
                sets.add(set);
            }

            matchResult.setSetScores(sets);
        }

        // Ak sú nastavené sety (či už klasické alebo z skreču), vyhodnotíme výsledok
        if (matchResult.getSetScores() != null && !matchResult.getSetScores().isEmpty()) {
            // Očíslujeme sety (1-based)
            for (int i = 0; i < matchResult.getSetScores().size(); i++) {
                matchResult.getSetScores().get(i).setSetNumber(i + 1);
            }

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

            // Určíme víťaza podľa setov
            if (setsWon1 > setsWon2) {
                String winnerId = switch (match.getMatchType()) {
                    case SINGLES -> match.getHomePlayer().getId();
                    case DOUBLES -> match.getHomeTeam().getId();
                };
                matchResult.setWinnerId(winnerId);
            } else if (setsWon2 > setsWon1) {
                String winnerId = switch (match.getMatchType()) {
                    case SINGLES -> match.getAwayPlayer().getId();
                    case DOUBLES -> match.getAwayTeam().getId();
                };
                matchResult.setWinnerId(winnerId);
            }
        }

        match.setResult(matchResult);
        match.setStatus(MatchStatus.FINISHED);
        return matchRepository.save(match);
    }

    @Override
    @Transactional
    public List<Match> generateMatchesForLeague(String leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("No League found with id: " + leagueId));

        MatchType type = league.getLeagueType();
        List<Match> matches;

        if (matchRepository.existsByLeagueId(leagueId)) {
            throw new IllegalStateException("Zápasy pre túto ligu už existujú!");
        }

        if (type == MatchType.SINGLES) {
            List<Player> players = league.getPlayers();
            matches = roundRobinPlayersService.generateMatches(new ArrayList<>(players), leagueId, type);
        } else if (type == MatchType.DOUBLES) {
            List<Team> teams = league.getTeams();
            matches = roundRobinTeamsService.generateMatches(new ArrayList<>(teams), leagueId, type);
        } else {
            throw new UnsupportedOperationException("Unsupported match type: " + type);
        }

        league.setStatus(LeagueStatus.ACTIVE);
        matchRepository.saveAll(matches);
        return matches;
    }

    @Override
    public List<Match> getMatchesForLeague(String leagueId) {
        return matchRepository.findByLeagueId(leagueId);
    }

    @Override
    public Map<Integer, List<Match>> getMatchesGroupedByRound(String leagueId) {
        List<Match> matches = matchRepository.findByLeagueId(leagueId);
        return matches.stream()
                .collect(Collectors.groupingBy(Match::getRoundNumber));
    }

    @Override
    public List<Match> getPlayedMatchesForLeague(String leagueId) {
        return matchRepository.findAllPlayedLeagueMatches(leagueId);
    }

    @Transactional
    @Override
    public void cancelResult(String matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("No Match found with id: " + matchId));

        match.setStatus(MatchStatus.CREATED);
        match.setResult(null);
        matchRepository.save(match);
    }
}
