package com.example.mauri.service;

import com.example.mauri.enums.LeagueStatus;
import com.example.mauri.enums.MatchType;
import com.example.mauri.model.*;
import com.example.mauri.model.dto.CreateLeagueDTO;
import com.example.mauri.repository.*;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LeagueServiceBean implements LeagueService {

    private final LeagueRepository leagueRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final SeasonRepository seasonRepository;

    public LeagueServiceBean(LeagueRepository leagueRepository, PlayerRepository playerRepository, TeamRepository teamRepository, MatchRepository matchRepository, SeasonRepository seasonRepository) {
        this.leagueRepository = leagueRepository;
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.seasonRepository = seasonRepository;
    }

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
                    .orElseThrow(()-> new IllegalArgumentException("Season not found with id: " + createLeagueDTO.getSeasonId()));
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

    @Transactional
    @Override
    public void removeParticipantFromLeague(String leagueId, String participantId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("No league found with id: " + leagueId));

        MatchType type = league.getLeagueType();

        switch (type) {
            case SINGLES -> {
                Player player = playerRepository.findById(participantId)
                        .orElseThrow(() -> new IllegalArgumentException("No player found with id: " + participantId));

                league.getPlayers().remove(player); // Bez kontroly, lebo remove nič nespraví ak tam nie je
            }
            case DOUBLES -> {
                Team team = teamRepository.findById(participantId)
                        .orElseThrow(() -> new IllegalArgumentException("No team found with id: " + participantId));

                league.getTeams().remove(team);
            }
        }

        leagueRepository.save(league);
    }

    @Override
    public List<League> getLeaguesWithoutSeason() {
        return leagueRepository.findBySeasonIsNull();
    }

}
