package com.example.mauri.service.impl;

import com.example.mauri.model.Player;
import com.example.mauri.model.Team;
import com.example.mauri.repository.PlayerRepository;
import com.example.mauri.repository.TeamRepository;
import com.example.mauri.service.TeamService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamServiceBean implements TeamService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;


    @Override
    public List<Team> getTeams() {
        return teamRepository.findAll().stream().toList();
    }

    @Override
    public Team getTeamById(@NonNull String id) {
        return teamRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("No team found with id: " + id));
    }

    @Override
    public Team createTeam(String player1Id, String player2Id) {
        Player player1 = playerRepository.findById(player1Id)
                .orElseThrow(()-> new IllegalArgumentException("No player found with id: " + player1Id));
        Player player2 = playerRepository.findById(player2Id)
                .orElseThrow(()-> new IllegalArgumentException("No player found with id: " + player2Id));

        Team team = new Team(UUID.randomUUID().toString(), player1, player2);
        teamRepository.save(team);
        return team;
    }
    @Override
    public void deleteTeam(@NonNull String id) {
        if (!teamRepository.existsById(id)) {
            throw new IllegalArgumentException("No team found with id: " + id);
        }
        teamRepository.deleteById(id);
    }

    @Override
    public List<Team> getTeamsNotInAnyActiveLeague() {
        return teamRepository.findTeamsNotInAnyActiveLeague();
    }
}
