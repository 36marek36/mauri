package com.example.mauri.service.volley.impl;

import com.example.mauri.exception.ResourceAlreadyExistsException;
import com.example.mauri.exception.ResourceNotFoundException;
import com.example.mauri.mapper.VolleyTeamMapper;
import com.example.mauri.model.Player;
import com.example.mauri.model.VolleyTeam;
import com.example.mauri.model.dto.request.VolleyTeamPlayerNameRequest;
import com.example.mauri.model.dto.response.VolleyTeamResponseDTO;
import com.example.mauri.repository.PlayerRepository;
import com.example.mauri.repository.VolleyLeagueRepository;
import com.example.mauri.repository.VolleyMatchRepository;
import com.example.mauri.repository.VolleyTeamRepository;
import com.example.mauri.service.volley.VolleyTeamService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VolleyTeamServiceBean implements VolleyTeamService {

    private final VolleyTeamRepository volleyTeamRepository;
    private final VolleyTeamMapper volleyTeamMapper;
    private final PlayerRepository playerRepository;
    private final VolleyLeagueRepository volleyLeagueRepository;
    private final VolleyMatchRepository volleyMatchRepository;

    @Override
    public List<VolleyTeamResponseDTO> getActiveVolleyTeams() {
        List<VolleyTeam> volleyTeams = volleyTeamRepository.findByActiveTrue();
        return volleyTeams.stream()
                .map(volleyTeamMapper::mapToResponseDTO)
                .toList();
    }

    @Override
    public List<VolleyTeamResponseDTO> getInactiveVolleyTeams() {
        List<VolleyTeam> volleyTeams = volleyTeamRepository.findByActiveFalse();
        return volleyTeams.stream()
                .map(volleyTeamMapper::mapToResponseDTO)
                .toList();
    }

    @Override
    public VolleyTeamResponseDTO getVolleyTeamById(String id) {
        return volleyTeamMapper.mapToResponseDTO(volleyTeamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Volley team not found")));
    }

    @Override
    public VolleyTeamResponseDTO createVolleyTeam(String teamName, String captainId) {
        Player captain = playerRepository.findById(captainId)
                .orElseThrow(() -> new ResourceNotFoundException("Captain not found"));

        boolean exists = volleyTeamRepository.existsByName(teamName);

        if (exists) {
            throw new ResourceAlreadyExistsException("Team already exists");
        }

        VolleyTeam volleyTeam = VolleyTeam.builder()
                .id(UUID.randomUUID().toString())
                .name(teamName)
                .captain(captain)
                .build();

        VolleyTeam saved = volleyTeamRepository.save(volleyTeam);
        return volleyTeamMapper.mapToResponseDTO(saved);
    }

    @Override
    public String deleteVolleyTeam(String id) {
        VolleyTeam volleyTeam = volleyTeamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Volley team not found"));

        boolean isInVolleyLeagues = !volleyLeagueRepository.findVolleyLeagueByVolleyTeamId(id).isEmpty();
        boolean isInVolleyMatch = volleyMatchRepository.existsByHomeTeamIdOrAwayTeamId(id, id);

        if (isInVolleyLeagues || isInVolleyMatch) {
            deactivateTeam(id);
            return "deactivated";
        } else {
            volleyTeamRepository.delete(volleyTeam);
            return "deleted";
        }
    }

    @Override
    public void addPlayerToTeam(String teamId, VolleyTeamPlayerNameRequest playerName) {
        VolleyTeam team = volleyTeamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Volley team not found"));

        boolean added = team.getPlayers().add(playerName.getPlayerName());

        if (!added) {
            throw new ResourceAlreadyExistsException("Player already exists in team");
        }

        log.info("Player {} added to team {}", playerName.getPlayerName(), teamId);

        volleyTeamRepository.save(team);
    }

    @Override
    public void removePlayerFromTeam(String teamId, VolleyTeamPlayerNameRequest playerName) {
        VolleyTeam team = volleyTeamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Volley team not found"));

        boolean removed = team.getPlayers().remove(playerName.getPlayerName());

        if (!removed) {
            throw new ResourceNotFoundException("Player not found in team");
        }

        log.info("Player {} removed from team {}", playerName.getPlayerName(), teamId);

        volleyTeamRepository.save(team);
    }

    @Override
    public void deactivateTeam(@NonNull String id) {
        VolleyTeam team = volleyTeamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Volley team not found"));
        team.setActive(false);
        volleyTeamRepository.save(team);
    }
}
