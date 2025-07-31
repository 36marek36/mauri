package com.example.mauri.service.impl;

import com.example.mauri.enums.LeagueStatus;
import com.example.mauri.enums.MatchStatus;
import com.example.mauri.enums.MatchType;
import com.example.mauri.model.*;
import com.example.mauri.model.dto.CreateLeagueDTO;
import com.example.mauri.model.dto.LeagueDTO;
import com.example.mauri.repository.*;
import com.example.mauri.service.LeagueService;
import com.example.mauri.service.MatchService;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeagueServiceBean implements LeagueService {

    private final LeagueRepository leagueRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final SeasonRepository seasonRepository;
    private final MatchService matchService;


    @Override
    public List<League> getAllLeagues() {
        return leagueRepository.findAll().stream().toList();
    }

    @Override
    public League getLeagueById(@NonNull String id) {
        return leagueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No league found with id: " + id));
    }

    @Override
    public League createLeague(CreateLeagueDTO createLeagueDTO) {
        Season season = null;

        if (createLeagueDTO.getSeasonId() != null && !createLeagueDTO.getSeasonId().isEmpty()) {
            season = seasonRepository.findById(createLeagueDTO.getSeasonId())
                    .orElseThrow(() -> new IllegalArgumentException("Season not found with id: " + createLeagueDTO.getSeasonId()));
        }
        League league = new League(
                UUID.randomUUID().toString(),
                createLeagueDTO.getName(),
                createLeagueDTO.getLeagueType(),
                season,
                new ArrayList<>(),
                new ArrayList<>(),
                LeagueStatus.CREATED);
        return leagueRepository.save(league);
    }

    @Override
    @Transactional
    public void deleteLeagueById(@NonNull String id) {
        League league = leagueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No league found with id: " + id));

        List<Match> matches = matchRepository.findByLeagueId(league.getId());
        matchRepository.deleteAll(matches);

        league.getPlayers().clear();
        league.getTeams().clear();

        leagueRepository.save(league);

        leagueRepository.deleteById(id);
    }

    @Override
    @Transactional
    public League addParticipantsToLeague(String leagueId, List<String> participantIds) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("No league found with id: " + leagueId));

        MatchType type = league.getLeagueType();

        switch (type) {
            case SINGLES -> {
                List<Player> players = playerRepository.findAllById(participantIds);
                for (Player player : players) {
                    if (!league.getPlayers().contains(player)) {
                        league.getPlayers().add(player);
                    }
                }
            }
            case DOUBLES -> {
                List<Team> teams = teamRepository.findAllById(participantIds);
                for (Team team : teams) {
                    if (!league.getTeams().contains(team)) {
                        league.getTeams().add(team);
                    }
                }
            }
        }

        return leagueRepository.save(league);
    }

    @Override
    @Transactional
    public void removeParticipantFromLeague(String leagueId, String participantId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("No league found with id: " + leagueId));

        MatchType type = league.getLeagueType();
        List<Match> affectedMatches;

        switch (type) {
            case SINGLES -> {
                Player player = playerRepository.findById(participantId)
                        .orElseThrow(() -> new IllegalArgumentException("No player found with id: " + participantId));

                affectedMatches = matchRepository.findByLeagueIdAndPlayer(leagueId, participantId);

                for (Match match : affectedMatches) {
                    MatchResult result = new MatchResult();
                    result.setScratchedId(participantId);

                    matchService.addResult(match.getId(), result);
                }

                league.getPlayers().remove(player);
            }

            case DOUBLES -> {
                Team team = teamRepository.findById(participantId)
                        .orElseThrow(() -> new IllegalArgumentException("No team found with id: " + participantId));

                affectedMatches = matchRepository.findByLeagueIdAndTeam(leagueId, participantId);

                for (Match match : affectedMatches) {
                    MatchResult result = new MatchResult();
                    result.setScratchedId(participantId);

                    matchService.addResult(match.getId(), result);
                }

                league.getTeams().remove(team);
            }

            default -> throw new UnsupportedOperationException("Unsupported match type: " + type);
        }

        leagueRepository.save(league);
    }

    @Override
    public List<League> getLeaguesWithoutSeason() {
        return leagueRepository.findBySeasonIsNull();
    }

    @Override
    public int progress(String leagueId) {
        int played = matchService.getPlayedMatchesForLeague(leagueId).size();
        int total = matchService.getMatchesForLeague(leagueId).size();

        if (total == 0) return 0;

        return (int) ((double) played / total * 100);
    }

    @Override
    public List<LeagueDTO> getLeaguesForPlayer(String playerId) {
        List<League> leagues = leagueRepository.findLeaguesByPlayerId(playerId);
        List<LeagueDTO> result = new ArrayList<>();

        for (League league : leagues) {
            String name = league.getName();
            Integer year = null;

            if (league.getSeason() != null) {
                year = league.getSeason().getYear();
            }

            result.add(new LeagueDTO(name, year));
        }

        return result;
    }

    @Override
    public void finishLeague(String leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("No league found with id: " + leagueId));

        if (league.getStatus().equals(LeagueStatus.FINISHED)) {
            throw new IllegalStateException("League already finished");
        }

        league.setStatus(LeagueStatus.FINISHED);
        leagueRepository.save(league);

        List<Match> matches = matchRepository.findByLeagueId(leagueId);
        for (Match match : matches) {
            if (match.getResult() != null &&
                    match.getResult().getScore1() != null &&
                    match.getResult().getScore2() != null &&
                    match.getResult().getWinnerId() != null) {
                match.setStatus(MatchStatus.FINISHED);
            } else {
                match.setStatus(MatchStatus.CANCELLED);
            }
        }
        matchRepository.saveAll(matches);
    }
}
