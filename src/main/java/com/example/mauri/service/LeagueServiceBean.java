package com.example.mauri.service;

import com.example.mauri.enums.MatchType;
import com.example.mauri.model.League;
import com.example.mauri.model.Player;
import com.example.mauri.model.Team;
import com.example.mauri.model.dto.CreateLeagueDTO;
import com.example.mauri.repository.LeagueRepository;
import com.example.mauri.repository.PlayerRepository;
import com.example.mauri.repository.TeamRepository;
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

    public LeagueServiceBean(LeagueRepository leagueRepository, PlayerRepository playerRepository, TeamRepository teamRepository) {
        this.leagueRepository = leagueRepository;
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
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
        League league = new League(
                UUID.randomUUID().toString(),
                createLeagueDTO.getLeagueName(),
                createLeagueDTO.getLeagueType(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());
        return leagueRepository.save(league);
    }

    @Override
    public void deleteLeagueById(@NonNull String id) {
        if (!leagueRepository.existsById(id)) {
            throw new IllegalArgumentException("No league found with id: " + id);
        }
        leagueRepository.deleteById(id);
    }

    @Override
    public League addParticipantToLeague(String leagueId, String participantId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("No league found with id: " + leagueId));

        MatchType type = league.getLeagueType();

        switch (type){
            case SINGLES -> {
                Player player = playerRepository.findById(participantId)
                        .orElseThrow(() -> new IllegalArgumentException("No player found with id: " + participantId));

                if (!league.getPlayers().contains(player)) {
                    league.getPlayers().add(player);
                }
            }
            case DOUBLES -> {
                Team team = teamRepository.findById(participantId)
                        .orElseThrow(()-> new IllegalArgumentException("No team found with id: "+ participantId));

                if (!league.getTeams().contains(team)) {
                    league.getTeams().add(team);
                }
            }
        }
        return leagueRepository.save(league);
    }

}
