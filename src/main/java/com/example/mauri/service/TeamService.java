package com.example.mauri.service;

import com.example.mauri.model.Team;
import com.example.mauri.model.dto.response.TeamResponseDTO;
import lombok.NonNull;

import java.util.List;

public interface TeamService {

    List<TeamResponseDTO> getActiveTeams();

    List<TeamResponseDTO> getInactiveTeams();

    Team getTeamById(@NonNull String id);

    TeamResponseDTO getTeamResponseById(String id);

    TeamResponseDTO createTeam(String player1Id, String player2Id);

    String deleteTeam(String id);

    List<TeamResponseDTO> getActiveTeamsNotInAnyActiveLeague();

    void deactivateTeam(@NonNull String id);

}
