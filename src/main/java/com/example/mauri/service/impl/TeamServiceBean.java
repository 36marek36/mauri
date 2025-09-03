package com.example.mauri.service.impl;

import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.model.Player;
import com.example.mauri.model.Team;
import com.example.mauri.model.dto.request.ParticipantDTO;
import com.example.mauri.model.dto.response.TeamResponseDTO;
import com.example.mauri.repository.LeagueRepository;
import com.example.mauri.repository.MatchRepository;
import com.example.mauri.repository.PlayerRepository;
import com.example.mauri.repository.TeamRepository;
import com.example.mauri.service.TeamService;
import com.example.mauri.util.ParticipantNameUtils;
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
    public List<TeamResponseDTO> getActiveTeams() {
        List<Team> teams = teamRepository.findByActiveTrue();
        return teams.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    public List<TeamResponseDTO> getInactiveTeams() {
        List<Team> teams = teamRepository.findByActiveFalse();
        return teams.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    public Team getTeamById(@NonNull String id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No team found with id: " + id));
    }

    @Override
    public TeamResponseDTO getTeamResponseById(String id) {
        Team team = getTeamById(id);
        return mapToResponseDTO(team);
    }

    @Override
    public TeamResponseDTO createTeam(String player1Id, String player2Id) {
        Player player1 = playerRepository.findById(player1Id)
                .orElseThrow(() -> new ResourceNotFoundException("No player found with id: " + player1Id));
        Player player2 = playerRepository.findById(player2Id)
                .orElseThrow(() -> new ResourceNotFoundException("No player found with id: " + player2Id));

        Team team = Team.builder()
                .id(UUID.randomUUID().toString())
                .player1(player1)
                .player2(player2)
                .build();
        Team saved = teamRepository.save(team);
        return mapToResponseDTO(saved);
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
    public List<TeamResponseDTO> getActiveTeamsNotInAnyActiveLeague() {
        List<Team> freeTeams = teamRepository.findActiveTeamsWithoutActiveLeague();
        return freeTeams.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    public void deactivateTeam(@NonNull String id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No team found with id: " + id));
        team.setDeletedDate(LocalDate.now());
        team.setActive(false);
        teamRepository.save(team);
    }

    @Override
    public void deactivateTeamsWithPlayer(@NonNull String playerId) {
        List<Team> teams = teamRepository.findByPlayer1IdOrPlayer2Id(playerId, playerId);
        for (Team team : teams) {
            deactivateTeam(team.getId());
        }
    }

    private TeamResponseDTO mapToResponseDTO (Team team){

        ParticipantDTO player1 = null;
        if (team.getPlayer1() != null) {
            String name1 = ParticipantNameUtils.buildPlayerName(team.getPlayer1());
            player1 = new ParticipantDTO(team.getPlayer1().getId(), name1,team.getPlayer1().isActive());
        }

        ParticipantDTO player2 = null;
        if (team.getPlayer2() != null) {
            String name2 = ParticipantNameUtils.buildPlayerName(team.getPlayer2());
            player2 = new ParticipantDTO(team.getPlayer2().getId(), name2,team.getPlayer2().isActive());
        }
        String teamName = ParticipantNameUtils.buildTeamName(team);

        return TeamResponseDTO.builder()
                .id(team.getId())
                .name(teamName)
                .player1(player1)
                .player2(player2)
                .active(team.isActive())
                .deletedDate(team.getDeletedDate())
                .registrationDate(team.getCreatedAt())
                .build();
    }
}
