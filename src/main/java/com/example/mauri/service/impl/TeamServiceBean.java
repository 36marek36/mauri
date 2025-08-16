package com.example.mauri.service.impl;

import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.model.Player;
import com.example.mauri.model.Team;
import com.example.mauri.repository.LeagueRepository;
import com.example.mauri.repository.MatchRepository;
import com.example.mauri.repository.PlayerRepository;
import com.example.mauri.repository.TeamRepository;
import com.example.mauri.service.TeamService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamServiceBean implements TeamService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final LeagueRepository leagueRepository;
    private final MatchRepository matchRepository;


    @Override
    public List<Team> getActiveTeams() {
        return teamRepository.findByActiveTrue();
    }

    @Override
    public List<Team> getInactiveTeams() {
        return teamRepository.findByActiveFalse();
    }

    @Override
    public Team getTeamById(@NonNull String id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No team found with id: " + id));
    }

    @Override
    public Team createTeam(String player1Id, String player2Id) {
        Player player1 = playerRepository.findById(player1Id)
                .orElseThrow(() -> new IllegalArgumentException("No player found with id: " + player1Id));
        Player player2 = playerRepository.findById(player2Id)
                .orElseThrow(() -> new IllegalArgumentException("No player found with id: " + player2Id));

        Team team = Team.builder()
                .id(UUID.randomUUID().toString())
                .player1(player1)
                .player2(player2)
                .build();
        teamRepository.save(team);
        return team;
    }

    @Override
    public String deleteTeam(String id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No team found with id: " + id));

        boolean isInLeagues = !leagueRepository.findLeaguesByTeamId(id).isEmpty();
        boolean isInMatch = matchRepository.existsByHomeTeamIdOrAwayTeamId(id, id);

        if (isInLeagues || isInMatch) {
            deactivateTeam(id);
            return "deactivated";
        } else {
            teamRepository.delete(team);
            return "deleted";
        }
    }

    @Override
    public List<Team> getActiveTeamsNotInAnyActiveLeague() {
        return teamRepository.findActiveTeamsWithoutActiveLeague();
    }

    @Override
    public void deactivateTeam(@NonNull String id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No team found with id: " + id));
        team.setDeletedDate(LocalDate.now());
        team.setActive(false);
        teamRepository.save(team);
    }

}
