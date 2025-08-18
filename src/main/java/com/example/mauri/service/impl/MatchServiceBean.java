package com.example.mauri.service.impl;

import com.example.mauri.enums.LeagueStatus;
import com.example.mauri.enums.MatchStatus;
import com.example.mauri.enums.MatchType;
import com.example.mauri.enums.SeasonStatus;
import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.model.*;
import com.example.mauri.model.dto.create.CreateMatchDTO;
import com.example.mauri.repository.*;
import com.example.mauri.service.MatchResultService;
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
    private final SeasonRepository seasonRepository;
    private final MatchResultService matchResultService;

    @Override
    public List<Match> getMatches() {
        return matchRepository.findAll().stream().toList();
    }

    @Override
    public Match getMatch(@NonNull String id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No Match found with id: " + id));
    }

    @Override
    public Match createMatch(CreateMatchDTO createMatchDTO) {
        Match match = Match.builder()
                .id(UUID.randomUUID().toString())
                .matchType(createMatchDTO.getMatchType())
                .leagueId(createMatchDTO.getLeagueId())
                .build();

        switch (createMatchDTO.getMatchType()) {
            case SINGLES -> {
                match.setHomePlayer(playerRepository.findById(createMatchDTO.getPlayer1Id())
                        .orElseThrow(() -> new ResourceNotFoundException("No Player found with id: " + createMatchDTO.getPlayer1Id())));
                match.setAwayPlayer(playerRepository.findById(createMatchDTO.getPlayer2Id())
                        .orElseThrow(() -> new ResourceNotFoundException("No Player found with id: " + createMatchDTO.getPlayer2Id())));
            }
            case DOUBLES -> {
                match.setHomeTeam(teamRepository.findById(createMatchDTO.getTeam1Id())
                        .orElseThrow(() -> new ResourceNotFoundException("No Team found with id: " + createMatchDTO.getTeam1Id())));
                match.setAwayTeam(teamRepository.findById(createMatchDTO.getTeam2Id())
                        .orElseThrow(() -> new ResourceNotFoundException("No Team found with id: " + createMatchDTO.getTeam2Id())));
            }
            default -> throw new IllegalArgumentException("Unsupported MatchType: " + createMatchDTO.getMatchType());
        }
        return matchRepository.save(match);
    }

    @Override
    public void deleteMatch(@NonNull String id) {
        if (!matchRepository.existsById(id)) {
            throw new ResourceNotFoundException("No Match found with id: " + id);
        }
        matchRepository.deleteById(id);
    }

    @Override
    public Match addResult(String matchId, MatchResult matchResult) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("No Match found with id: " + matchId));

        MatchResult finalResult = matchResultService.processResult(match, matchResult);
        match.setResult(finalResult);
        match.setStatus(MatchStatus.FINISHED);
        return matchRepository.save(match);
    }

    @Override
    @Transactional
    public List<Match> generateMatchesForLeague(String leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("No League found with id: " + leagueId));

        MatchType type = league.getLeagueType();
        List<Match> matches;

        if (matchRepository.existsByLeagueId(leagueId)) {
            throw new IllegalStateException("Zápasy pre ligu '" + league.getName() + "' už existujú!");
        }

        if (type == MatchType.SINGLES) {
            List<Player> players = league.getPlayers();
            if (players.size() < 2) {
                throw new IllegalStateException("Liga '" + league.getName() + "' musí obsahovať aspoň 2 hráčov.");
            }
            matches = roundRobinPlayersService.generateMatches(new ArrayList<>(players), leagueId, type);
        } else if (type == MatchType.DOUBLES) {
            List<Team> teams = league.getTeams();
            if (teams.size() < 2) {
                throw new IllegalStateException("Liga '" + league.getName() + "' musí obsahovať aspoň 2 tímy.");
            }
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
                .orElseThrow(() -> new ResourceNotFoundException("No Match found with id: " + matchId));

        match.setStatus(MatchStatus.CREATED);
        match.setResult(null);
        matchRepository.save(match);
    }

    @Override
    public List<Match> getMatchesForPlayerInActiveSeason(String playerId, MatchStatus status) {
        List<String> leagueIds = getActiveSeasonLeagueIds();
        if (leagueIds.isEmpty()) {
            return new ArrayList<>();
        }
        return matchRepository.findByPlayerStatusAndLeagueIds(playerId, status, leagueIds);
    }

    @Override
    public List<Match> getMatchesForTeamInActiveSeason(String teamId, MatchStatus status) {
        List<String> leagueIds = getActiveSeasonLeagueIds();
        if (leagueIds.isEmpty()) {
            return new ArrayList<>();
        }
        return matchRepository.findByTeamStatusAndLeagueIds(teamId, status, leagueIds);
    }

    private List<String> getActiveSeasonLeagueIds() {
        Season activeSeason = seasonRepository.findByStatus(SeasonStatus.ACTIVE).orElse(null);

        if (activeSeason == null) {
            return new ArrayList<>();
        }

        List<String> leagueIds = new ArrayList<>();
        for (League league : activeSeason.getLeagues()) {
            leagueIds.add(league.getId());
        }
        return leagueIds;
    }
}
